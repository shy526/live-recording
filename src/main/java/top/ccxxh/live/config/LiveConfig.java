package top.ccxxh.live.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 配置
 * @author qing
 */
@Component
@ConfigurationProperties(prefix = "live")
public class LiveConfig {
    private Map<String,List<Integer>>  roomIdGroup;
    private String rootPath;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, List<Integer>> getRoomIdGroup() {
        return roomIdGroup;
    }

    public void setRoomIdGroup(Map<String, List<Integer>> roomIdGroup) {
        this.roomIdGroup = roomIdGroup;
    }
}
