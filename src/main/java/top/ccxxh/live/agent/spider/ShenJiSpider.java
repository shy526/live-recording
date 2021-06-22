package top.ccxxh.live.agent.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import top.ccxxh.live.agent.AgentIp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * http://www.shenjidaili.com/product/open/
 *
 * @author qing
 */
@Component
public class ShenJiSpider extends AbsIpSpider {
    private final List<String> urls = Arrays.asList("http://www.shenjidaili.com/product/open/");

    @Override
    protected Integer getAgentIps(Queue<AgentIp> queue, String url) {
        Integer result = 0;
        try {
            Document document = Jsoup.connect(url).get();
            Elements tbody = document.getElementsByTag("tbody");
            for (Element element : tbody) {
                Elements trs = element.getElementsByTag("tr");
                for (int i = 1; i < trs.size(); i++) {
                    Element td = trs.get(i).getElementsByTag("td").get(0);
                    final String[] split = td.text().split(":");
                    if (split.length < 2) {
                        continue;
                    }
                    AgentIp agentIp = new AgentIp(split[0], Integer.parseInt(split[1]));
                    agentIp.setSource(this.getClass().getSimpleName());
                    queue.add(agentIp);
                    result++;
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }

}
