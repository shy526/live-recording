package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.ccxxh.live.agent.AgentIp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * https://superfastip.com/#/
 *
 * @author qing
 */
@Component
public class SuperFastIpSpider extends AbsIpSpider {
    private final List<String> urls = Arrays.asList("https://api.superfastip.com/ip/freeip?page=%s");

    @Override
    protected Integer getAgentIps(Queue<AgentIp> queue, String url) {
        Integer result = 0;
        for (int i = 1; i < 10; i++) {
            try {
                Document document = Jsoup.connect(String.format(url, i)).get();
                String text = document.text();
                JSONObject json = JSON.parseObject(text);
                JSONArray ips = json.getJSONArray("freeips");
                for (Object o : ips) {
                    JSONObject item = (JSONObject) o;
                    Integer port = item.getInteger("port");
                    String ip = item.getString("ip");
                    AgentIp agentIp = new AgentIp(ip, port);
                    agentIp.setSource(this.getClass().getSimpleName());
                    queue.add(agentIp);
                    result++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected List<String> getUrls() {
        return urls;
    }
}
