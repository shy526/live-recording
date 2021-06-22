package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.agent.AgentManager;

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
public class KuaiSpider extends AbsIpSpider {
    private final List<String> urls = Arrays.asList("https://www.kuaidaili.com/free/inha/%s/", "https://www.kuaidaili.com/free/intr/%s/");

    @Autowired
    private AgentManager agentManager;

    @Override
    public Integer getAgentIps(Queue<AgentIp> queue, String url) {
        AtomicInteger result = new AtomicInteger();
        for (int i = 1; i <= 100; i++) {
            String url2 = String.format(url, i);
            agentManager.leaseAgentTake(agentIp -> {
                try {
                    System.out.println("JSON.toJSONString(agentIp) = " + JSON.toJSONString(agentIp));
                    Document document = Jsoup.connect(url2).proxy(agentIp.getIp(), agentIp.getPort()).get();
                    result.addAndGet(parseTbody(queue, document, 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, false);

        }
        return result.get();
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }
}
