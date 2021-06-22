package top.ccxxh.live.agent;

import com.alibaba.fastjson.JSON;
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
    private final Queue<AgentIp> testQueue = new LinkedList<>();
    private final Queue<AgentIp> successQueue = new LinkedList<>();
    private final Queue<AgentIp> highQueue = new LinkedList<>();
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
                item.getAgentIps(testQueue);
            });
        });
    }


    private void testSuccessAgentIp() {
        testAgentIp(successQueue);
    }

    private void testAgentIp(Queue<AgentIp> queue) {
        successPoll.execute(() -> {
            while (true) {
                AgentIp agentIp = queue.poll();
                if (agentIp != null) {
                    if (System.currentTimeMillis() - agentIp.getTestTime() > 1000 * 60 * 5) {
                        returnAgentIp(agentIp, agentIp.getTestUrl());
                        log.info("二次检测:{}", JSON.toJSONString(agentIp));
                    } else {
                        queue.add(agentIp);
                    }
                }
                try {
                    Thread.sleep(500);
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
                AgentIp agentIp = testQueue.poll();
                if (agentIp != null) {
                    testAgentPool.execute(() -> {
                        if (!bloomFilter.mightContain(agentIp.getString())) {
                            bloomFilter.put(agentIp.getString());
                            returnAgentIp(agentIp, "https://api.live.bilibili.com/room/v1/Room/room_init?id=22528847");
                        }
                    });
                } else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
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
                log.info("优质代理:{}", JSON.toJSONString(agentIp));
            } else if (agentIp.getTestRepTime() != -1 && agentIp.getTestRepTime() <= 5000) {
                successQueue.add(agentIp);
                log.info("可用代理:{}", JSON.toJSONString(agentIp));
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
            if (httpResult != null) {
                try {
                    httpResult.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    public AgentIp getAgent() {
        AgentIp result = highQueue.poll();
        if (result == null) {
            result = successQueue.poll();
        }
        if (successQueue.size() < MIN_AGENT_NUMBER) {
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
        AgentIp agent = getAgent();
        handle.process(agent);
        returnAgentIp(agent, agent.getTestUrl());
    }
}
