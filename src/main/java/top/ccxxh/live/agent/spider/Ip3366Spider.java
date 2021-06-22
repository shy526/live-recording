package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.agent.AgentManager;

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
public class Ip3366Spider extends AbsIpSpider {
    private final List<String> urls = Arrays.asList("http://www.ip3366.net/free/?stype=1&page=%s", "http://www.ip3366.net/free/?stype=2&page=%s");
    @Autowired
    private AgentManager agentManager;

    @Override
    protected Integer getAgentIps(Queue<AgentIp> queue, String url) {
        AtomicInteger result = new AtomicInteger();
        for (int i = 1; i <= 7; i++) {
            String url2 = String.format(url, i);
            agentManager.leaseAgentTake(agentIp -> {
                try {
                    Document document = Jsoup.connect(url2).proxy(agentIp.getIp(), agentIp.getPort()).get();
                    result.addAndGet(parseTbody(queue, document, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, false);
            System.out.println("url2 = " + url2+"---------"+result.get());
        }
        return result.get();
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }
}
