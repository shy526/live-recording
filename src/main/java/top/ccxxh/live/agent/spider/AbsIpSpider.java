package top.ccxxh.live.agent.spider;

import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.agent.AgentIp;

import java.util.Queue;

/**
 * @author qing
 */
public abstract class AbsIpSpider {


    /**
     * 获取ip列表
     *
     * @return List<AgentIp>
     */
    public abstract Queue<AgentIp>  getAgentIps(Queue<AgentIp> queue);



}
