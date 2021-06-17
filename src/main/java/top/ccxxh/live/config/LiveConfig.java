package top.ccxxh.live.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配置
 * @author qing
 */
@Component
@ConfigurationProperties(prefix = "live")
public class LiveConfig {
    private List<Integer> bili;
    private List<Integer> mis;
    private String rootPath;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public List<Integer> getBili() {
        return bili;
    }

    public void setBili(List<Integer> bili) {
        this.bili = bili;
    }

    public List<Integer> getMis() {
        return mis;
    }

    public void setMis(List<Integer> mis) {
        this.mis = mis;
    }
}
