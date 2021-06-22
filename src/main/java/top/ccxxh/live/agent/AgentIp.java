package top.ccxxh.live.agent;

import java.io.Serializable;

/**
 * @author qing
 */
public class AgentIp implements Serializable {
    private String ip;
    private Integer port;
    private Long testTime;
    private String testUrl;
    private Long testRepTime;
    private String source;
    private Integer lv;

    public Integer getLv() {
        return lv;
    }

    public void setLv(Integer lv) {
        this.lv = lv;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getTestRepTime() {
        return testRepTime;
    }

    public void setTestRepTime(Long testRepTime) {
        this.testRepTime = testRepTime;
    }

    public AgentIp(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getTestTime() {
        return testTime;
    }

    public void setTestTime(Long testTime) {
        this.testTime = testTime;
    }

    public String getString(){
        return ip+":"+port;
    }
}
