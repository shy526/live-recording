package top.ccxxh.live.agent.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.agent.AgentManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * http://www.66ip.cn/areaindex_1/1.html
 *会封
 * @author qing
 */
@Component
public class Ip66Spider extends AbsIpSpider {

    private final List<String> urls = Arrays.asList("http://www.66ip.cn/nmtq.php?getnum=299&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=1&proxytype=2&api=66ip");
    @Override
    public Integer getAgentIps(Queue<AgentIp> queue, String url) {
        Integer result=0;
        for (int i = 0; i < 10; i++) {
            try {
                Document document = Jsoup.connect(url).get();
                final String[] ips = document.body().text().split(" ");
                for (String ip : ips) {
                    final String[] split = ip.split(":");
                    if (split.length < 2) {
                        continue;
                    }
                    AgentIp agentIp = new AgentIp(split[0], Integer.parseInt(split[1]));
                    agentIp.setSource(this.getClass().getSimpleName());
                    queue.add(agentIp);
                    result++;
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }

}
