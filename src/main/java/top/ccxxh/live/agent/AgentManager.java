package top.ccxxh.live.agent;

import com.alibaba.fastjson.JSON;
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
    private final ThreadPoolExecutor testAgentPool = ThreadPoolUtils.getThreadPool("test_agent");
    private final ThreadPoolExecutor spiderPool = ThreadPoolUtils.getThreadPool("ip_spider");
    private final ThreadPoolExecutor toolPool = ThreadPoolUtils.getThreadPool("toolPool");
    private final Queue<AgentIp> testQueue = new LinkedList<>();
    private final Queue<AgentIp> successQueue = new LinkedList<>();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("testAgent")
    private HttpClientService httpClientService;
    @Autowired
    private ApplicationContext applicationContext;

    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    private void ipSpiderScheduled() {
        log.info("ipSpiderScheduled start");
        Map<String, AbsIpSpider> beansOfType = applicationContext.getBeansOfType(AbsIpSpider.class);
        beansOfType.forEach((key, item) -> {
            spiderPool.execute(() -> {
                item.getAgentIps(testQueue);
            });
        });
    }

    public void testStart() {
        toolPool.execute(() -> {
            while (true) {
                AgentIp agentIp = testQueue.poll();
                if (agentIp != null) {
                    testAgentPool.execute(() -> {
                        if (testAgentIp(agentIp, "https://api.live.bilibili.com/room/v1/Room/room_init?id=22528847")) {
                            successQueue.add(agentIp);
                        }
                    });
                }else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {}
                }
            }
        });
    }

    public boolean testAgentIp(AgentIp agentIp, String url) {
        HttpRequestBase httpRequestBase = httpClientService.buildProxyGet(agentIp.getIp(), agentIp.getPort(), url, null, null);
        boolean result = false;
        HttpResult httpResult = null;
        agentIp.setTestTime(System.currentTimeMillis());
        agentIp.setTestRepTime(-1L);
        agentIp.setTestUrl(url);
        try {
            httpResult = httpClientService.execute(httpRequestBase,false);
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
}
