package top.ccxxh.live.agent.spider;

import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxxh.live.agent.AgentIp;
import top.ccxxh.live.recording.FlvRecording;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

/**
 * @author qing
 */
public abstract class AbsIpSpider {
    private final static Logger log = LoggerFactory.getLogger(FlvRecording.class);

    /**
     * 获取ip列表
     *
     * @param queue 参数
     * @return Queue<AgentIp>
     */
    protected abstract Integer getAgentIps(Queue<AgentIp> queue, String url);

    /**
     * 多url处理
     *
     * @param queue queue
     * @return Queue<AgentIp>
     */
    public Integer getAgentByUrls(Queue<AgentIp> queue) {
        Integer result = 0;
        for (String url : getUrls()) {
            result += getAgentIps(queue, url);
        }

        return result;
    }


    /**
     * 获取目标地址
     *
     * @return String url
     */
    protected abstract List<String> getUrls();

    protected Integer parseTbody(Queue<AgentIp> queue, Document document, int index) {
        Integer result = 0;
        Elements tbody = document.getElementsByTag("tbody");
        for (Element element : tbody) {
            Elements trs = element.getElementsByTag("tr");
            for (int j = index; j < trs.size(); j++) {
                Element ip = trs.get(j).getElementsByTag("td").get(0);
                Element port = trs.get(j).getElementsByTag("td").get(1);
                AgentIp agentIp = new AgentIp(ip.text().trim(), Integer.parseInt(port.text()));
                agentIp.setSource(this.getClass().getSimpleName());
                queue.add(agentIp);
                result++;
            }
        }
        return result;
    }
}
