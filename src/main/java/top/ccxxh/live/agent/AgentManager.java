package top.ccxxh.live.agent;

import com.alibaba.fastjson.util.IOUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxh.httpclient.tool.ThreadPoolUtils;
import top.ccxxh.live.agent.spider.AbsIpSpider;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author qing
 */
@Component
public class AgentManager implements CreatePool {
    private final static Logger log = LoggerFactory.getLogger(AgentManager.class);
    /**
     * 获取代理时 代理池 小于 MIN_AGENT_NUMBER 启动spider
     */
    private final static int MIN_AGENT_NUMBER = 20;
    private final static int MIN_HIGH = 1000;
    private final static int MIN_SUCCESS = 3000;
    private final static String HIGH = "%s-high";
    private final static String SUCCESS = "%s-success";


    /**
     * 爬虫的线程池
     */
    private final ThreadPoolExecutor ipSpiderPool = ThreadPoolUtils.getThreadPool("ipSpiderPool");
    /**
     * 其他使用的线程池
     */
    private final ThreadPoolExecutor toolPool = ThreadPoolUtils.getThreadPool("toolPool");

    /**
     * 循环检测用的线程池
     */
    private final ThreadPoolExecutor repeatCheckPoll = ThreadPoolUtils.getThreadPool("repeatCheck");

    /**
     * 等待参与海选的ip
     */
    private final BlockingQueue<AgentIp> auditionQueue = new LinkedBlockingQueue<>();
    /**
     * 海选筛选的线程池
     */
    private final ThreadPoolExecutor auditionPool = ThreadPoolUtils.getThreadPool("auditionPool");

    /**
     * 代理池细分 降低 请求错误的可能
     */
    private final Map<String, BlockingQueue<AgentIp>> agentPool = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    private final BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), Integer.MAX_VALUE);
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("testAgent")
    private HttpClientService httpClientService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 每五分钟抓取一次agentIP 后期时间拉长
     */
    @Scheduled(cron = "0 */30 * * * ?")
    private void ipSpiderScheduled() {
        log.info("ipSpiderScheduled start");
        Map<String, AbsIpSpider> beansOfType = applicationContext.getBeansOfType(AbsIpSpider.class);
        beansOfType.forEach((key, item) -> {
            ipSpiderPool.execute(() -> {
                Integer total = item.getAgentByUrls(auditionQueue);
                log.info("spider info:{}-{}", key, total);
            });
        });
    }

    private void QueueRepeatCheck(BlockingQueue<AgentIp> queue, String key) {
        repeatCheckPoll.execute(() -> {
            int max = 1000 * 60 * 2;
            while (true) {
                try {
                    AgentIp agentIp = queue.take();
                    long diff = System.currentTimeMillis() - agentIp.getTestTime();
                    if (diff >= max) {
                        Integer lv = agentIp.getLv();
                        returnAgentIp(agentIp, agentIp.getTestUrl(), key);
                    } else {
                        queue.add(agentIp);
                        Thread.sleep(max - diff);
                    }
                } catch (InterruptedException e) {
                }
            }
        });
    }

    /**
     * 初始化循环检测
     */
    public void initRepeatCheck() {
        Map<String, CreatePool> beansOfType = applicationContext.getBeansOfType(CreatePool.class);
        beansOfType.entrySet().forEach(item -> {
            CreatePool value = item.getValue();
            String key = value.getClass().getSimpleName();
            BlockingQueue<AgentIp> highQueue = agentPool.get(String.format(HIGH, key));
            BlockingQueue<AgentIp> successQueue = agentPool.get(String.format(SUCCESS, key));
            QueueRepeatCheck(highQueue, key);
            QueueRepeatCheck(successQueue, key);
        });
    }

    /**
     * 初始化海选检测
     */
    public void initAuditionCheck() {
        toolPool.execute(() -> {
            while (true) {
                try {
                    AgentIp agentIp = auditionQueue.take();
                    if (!bloomFilter.mightContain(agentIp.getString())) {
                        bloomFilter.put(agentIp.getString());
                        Map<String, CreatePool> beansOfType = applicationContext.getBeansOfType(CreatePool.class);
                        beansOfType.entrySet().forEach(item -> {
                            CreatePool value = item.getValue();
                            AgentIp copy = agentIp.copy();
                            auditionPool.execute(() -> {
                                returnAgentIp(copy, value.getCheckUrl(), value.getClass().getSimpleName());
                            });
                        });

                    }
                } catch (Exception e) {
                }
            }
        });
    }


    /**
     * 启动初步赛选的线程
     * 和第一次爬虫
     */
    @PostConstruct
    public void initAgentManager() {
        initPool();
        ipSpiderScheduled();
        initRepeatCheck();
        initAuditionCheck();
    }


    private void initPool() {
        Map<String, CreatePool> beansOfType = applicationContext.getBeansOfType(CreatePool.class);
        beansOfType.entrySet().forEach(item -> {
            CreatePool value = item.getValue();
            String simpleName = value.getClass().getSimpleName();
            String highPool = String.format(HIGH, simpleName);
            String successPool = String.format(SUCCESS, simpleName);
            agentPool.put(highPool, new LinkedBlockingQueue<>());
            agentPool.put(successPool, new LinkedBlockingQueue<>());
        });
    }

    /**
     * 归还agentIP
     *
     * @param agentIp agentIp
     * @param url     测试的url 连接
     */
    public void returnAgentIp(AgentIp agentIp, String url, String key) {
        if (checkAgentIp(agentIp, url)) {
            if (agentIp.getTestRepTime() <= MIN_HIGH) {
                String highKey = String.format(HIGH, key);
                agentPool.get(highKey).add(agentIp);
                agentIp.setLv(1);
                agentIp.setQueueSource(highKey);
            } else if (agentIp.getTestRepTime() != -1 && agentIp.getTestRepTime() <= MIN_SUCCESS) {
                String successKey = String.format(SUCCESS, key);
                agentPool.get(successKey).add(agentIp);
                agentIp.setQueueSource(successKey);
                agentIp.setLv(0);
            }
        }
    }

    public boolean checkAgentIp(AgentIp agentIp, String url) {
        HttpRequestBase httpRequestBase = httpClientService.buildProxyGet(agentIp.getIp(), agentIp.getPort(), url, null, null);
        boolean result = false;
        HttpResult httpResult = null;
        agentIp.setTestRepTime(-1L);
        agentIp.setTestUrl(url);
        try {
            agentIp.setTestTime(System.currentTimeMillis());
            httpResult = httpClientService.execute(httpRequestBase, false);
            if (httpResult != null) {
                result = httpResult.getHttpStatus().equals(HttpStatus.SC_OK);
            }
            if (result) {
                agentIp.setTestRepTime(System.currentTimeMillis() - agentIp.getTestTime());
            }
        } catch (Exception e) {
        } finally {
            IOUtils.close(httpResult);
        }
        return result;
    }

    public AgentIp getAgent(boolean flag, String key) {
        final String highKey = String.format(HIGH, key);
        final String successKey = String.format(SUCCESS, key);
        BlockingQueue<AgentIp> highQueue = agentPool.get(highKey);
        BlockingQueue<AgentIp> successQueue = agentPool.get(successKey);
        AgentIp result = highQueue.poll();
        if (result == null) {
            result = successQueue.poll();
        }
        if (flag && flag && successQueue.size() + highQueue.size() < MIN_AGENT_NUMBER) {
            log.info("poll size{}:{},{}:{}", highKey, highQueue.size(), successKey, successQueue.size());
            ipSpiderScheduled();
        }
        return result;
    }

    public AgentIp getAgentTake(boolean flag, String key) {
        final String highKey = String.format(HIGH, key);
        final String successKey = String.format(SUCCESS, key);
        BlockingQueue<AgentIp> highQueue = agentPool.get(highKey);
        BlockingQueue<AgentIp> successQueue = agentPool.get(successKey);
        AgentIp result = highQueue.poll();
        if (result == null) {
            try {
                result = successQueue.take();
            } catch (InterruptedException e) {
            }
        }
        if (flag && successQueue.size() + highQueue.size() < MIN_AGENT_NUMBER) {
            log.info("poll size{}:{},{}:{}", highKey, highQueue.size(), successKey, successQueue.size());
            ipSpiderScheduled();
        }
        return result;
    }

    /**
     * 租借AgentIp
     *
     * @param handle 处理器
     */
    public void leaseAgent(LeaseAgentHandle handle, String key) {
        leaseAgent(handle, true, key);
    }

    public void leaseAgent(LeaseAgentHandle handle, boolean flag, String key) {
        AgentIp agent = getAgent(flag, key);
        handle.process(agent);
        returnAgentIp(agent, agent.getTestUrl(), key);
    }


    public void leaseAgentTake(LeaseAgentHandle handle, boolean flag, String key) {
        AgentIp agent = getAgentTake(flag, key);
        handle.process(agent);
        returnAgentIp(agent, agent.getTestUrl(), key);
    }

    @Override
    public String getCheckUrl() {
        return "https://www.baidu.com/";
    }
}
