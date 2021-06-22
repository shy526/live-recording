package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.agent.AgentIp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * http://www.66ip.cn/areaindex_1/1.html
 *
 * @author qing
 */
@Component
public class Ip66Spider extends AbsIpSpider {
    private String page_html = "http://www.66ip.cn/nmtq.php?getnum=299&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=1&proxytype=2&api=66ip";


    @Override
    public Queue<AgentIp> getAgentIps(Queue<AgentIp> queue) {
        Queue<AgentIp> result = new LinkedList<>();
        if (queue != null) {
            result = queue;
        }
        for (int i = 0; i < 10; i++) {
            partsHtml(result);
        }
        return result;
    }

    private void partsHtml(Queue<AgentIp> result) {
        try {
            Document document = Jsoup.connect(page_html).get();
            final String[] ips = document.body().text().split(" ");
            for (String ip : ips) {
                final String[] split = ip.split(":");
                if (split.length<2){
                    continue;
                }
                AgentIp agentIp = new AgentIp(split[0], Integer.parseInt(split[1]));
                result.add(agentIp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
