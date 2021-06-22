package top.ccxxh.live.agent;

import com.alibaba.fastjson.JSON;
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
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author qing
 */
@Component
public class AgentManager {
    private final static Logger log = LoggerFactory.getLogger(AgentManager.class);
    private final static int MIN_AGENT_NUMBER = 20;
    private final ThreadPoolExecutor testAgentPool = ThreadPoolUtils.getThreadPool("test_agent");
    private final ThreadPoolExecutor spiderPool = ThreadPoolUtils.getThreadPool("ip_spider");
    private final ThreadPoolExecutor toolPool = ThreadPoolUtils.getThreadPool("toolPool");
    private final ThreadPoolExecutor successPoll = ThreadPoolUtils.getThreadPool("testSuccess");
    private final BlockingQueue<AgentIp> testQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<AgentIp> successQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<AgentIp> highQueue = new LinkedBlockingQueue<>();
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
    @Scheduled(cron = "0 */5 * * * ?")
    private void ipSpiderScheduled() {
        log.info("ipSpiderScheduled start");
        Map<String, AbsIpSpider> beansOfType = applicationContext.getBeansOfType(AbsIpSpider.class);
        beansOfType.forEach((key, item) -> {
            spiderPool.execute(() -> {
                Integer total = item.getAgentByUrls(testQueue);
                log.info("spider info:{}-{}", key,total);
            });
        });
    }


    private void testSuccessAgentIp() {
        testAgentIp(successQueue);
    }

    private void testAgentIp(BlockingQueue<AgentIp> queue) {
        successPoll.execute(() -> {
            while (true) {
                try {
                    AgentIp agentIp = queue.take();
                    long diff = System.currentTimeMillis() - agentIp.getTestTime();
                    if (diff >= 1000 * 60 * 5) {
                        Integer lv = agentIp.getLv();
                        returnAgentIp(agentIp, agentIp.getTestUrl());
                        log.info("lvTest={}:{}->{}",agentIp.getString(),lv,agentIp.getLv());
                    } else {
                        queue.add(agentIp);
                        Thread.sleep(1000 * 60 * 5 - diff);
                    }
                } catch (InterruptedException e) {
                }
            }
        });

    }

    private void testHighAgentIp() {
        testAgentIp(highQueue);
    }

    /**
     * 启动初步赛选的线程
     * 和第一次爬虫
     */
    @PostConstruct
    public void testStart() {
        ipSpiderScheduled();
        toolPool.execute(() -> {
            while (true) {
                try {
                    AgentIp agentIp = testQueue.take();
                    if (!bloomFilter.mightContain(agentIp.getString())) {
                        bloomFilter.put(agentIp.getString());
                        testAgentPool.execute(() -> {
                            returnAgentIp(agentIp, "https://fm.missevan.com/api/v2/live/133817151");
                        });
                    }
                } catch (Exception e) {
                }
            }
        });
        testSuccessAgentIp();
        testHighAgentIp();
    }

    /**
     * 归还agentIP
     *
     * @param agentIp agentIp
     * @param url     测试的url 连接
     */
    public void returnAgentIp(AgentIp agentIp, String url) {
        if (testAgentIp(agentIp, url)) {
            if (agentIp.getTestRepTime() <= 1000) {
                highQueue.add(agentIp);
                agentIp.setLv(1);
            } else if (agentIp.getTestRepTime() != -1 && agentIp.getTestRepTime() <= 5000) {
                successQueue.add(agentIp);
                agentIp.setLv(0);
            }
        }
    }

    public boolean testAgentIp(AgentIp agentIp, String url) {
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

    public AgentIp getAgent(boolean flag) {
        AgentIp result = highQueue.poll();
        if (result == null) {
            result = successQueue.poll();
        }
        if (flag&&successQueue.size() < MIN_AGENT_NUMBER) {
            ipSpiderScheduled();
        }
        return result;
    }
    public AgentIp getAgentTake(boolean flag) {
        AgentIp result = highQueue.poll();
        if (result == null) {
            try {
                result = successQueue.take();
            } catch (InterruptedException e) {
            }
        }
        if (flag&&successQueue.size() < MIN_AGENT_NUMBER) {
            ipSpiderScheduled();
        }
        return result;
    }

    /**
     * 租借AgentIp
     *
     * @param handle 处理器
     */
    public void leaseAgent(LeaseAgentHandle handle) {
            leaseAgent(handle,true);
    }
    public void leaseAgent(LeaseAgentHandle handle,boolean flag) {
        AgentIp agent = getAgent(flag);
        handle.process(agent);
        returnAgentIp(agent, agent.getTestUrl());
    }


    public void leaseAgentTake(LeaseAgentHandle handle,boolean flag) {
        AgentIp agent = getAgentTake(flag);
        handle.process(agent);
        returnAgentIp(agent, agent.getTestUrl());
    }
}
