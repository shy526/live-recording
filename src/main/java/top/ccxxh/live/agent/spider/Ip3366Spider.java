package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.agent.AgentManager;
import top.ccxxh.live.agent.CreatePool;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * http://www.ip3366.net/free/?stype=1&page=1
 *
 * @author qing
 */
@Component
public class Ip3366Spider extends AbsIpSpider implements CreatePool {
    private final List<String> urls = Arrays.asList("http://www.ip3366.net/free/?stype=1&page=%s", "http://www.ip3366.net/free/?stype=2&page=%s");
    @Autowired
    private AgentManager agentManager;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("testAgent")
    private HttpClientService httpClientService;

    @Override
    protected Integer getAgentIps(Queue<AgentIp> queue, String url) {
        AtomicInteger result = new AtomicInteger();
        for (int i = 1; i <= 7; i++) {
            String url2 = String.format(url, i);
            agentManager.leaseAgentTake(agentIp -> {
                HttpResult httpResult = null;
                try {
                    HttpRequestBase httpRequest = httpClientService.buildProxyGet(agentIp.getIp(), agentIp.getPort(),
                            url2, null, null);
                    httpResult = httpClientService.execute(httpRequest, false);
                    if (httpResult != null&&httpResult.getEntityStr()!=null) {
                        String entityStr = httpResult.getEntityStr();
                        result.addAndGet(parseTbody(queue, Jsoup.parse(entityStr), 0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(httpResult);
                }
            }, false, this.getClass().getSimpleName());
        }
        return result.get();
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }

    @Override
    public String getCheckUrl() {
        return "http://www.ip3366.net/free/?stype=1&page=1";
    }
}
