package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.agent.AgentManager;
import top.ccxxh.live.agent.CreatePool;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://www.kuaidaili.com/free/inha/2/
 *
 * @author qing
 */
@Component
public class KuaiSpider extends AbsIpSpider implements CreatePool {
    private final List<String> urls = Arrays.asList("https://www.kuaidaili.com/free/inha/%s/", "https://www.kuaidaili.com/free/intr/%s/");
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("testAgent")
    private HttpClientService httpClientService;
    @Autowired
    private AgentManager agentManager;

    @Override
    public Integer getAgentIps(Queue<AgentIp> queue, String url) {
        AtomicInteger result = new AtomicInteger();
        for (int i = 1; i <= 10; i++) {
            String url2 = String.format(url, i);

            agentManager.leaseAgentTake(agentIp -> {
                HttpResult httpResult = null;
                try {
                    HttpRequestBase httpRequest = httpClientService.buildProxyGet(agentIp.getIp(), agentIp.getPort(),
                            url2, null, null);
                    httpResult = httpClientService.execute(httpRequest, false);
                    if (httpResult != null&&httpResult.getEntityStr()!=null) {
                        String entityStr = httpResult.getEntityStr();
                        result.addAndGet(parseTbody(queue, Jsoup.parse(entityStr), 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        return "https://www.kuaidaili.com/free/inha/2/";
    }
}
