package top.ccxxh.live.agent;

/**
 * 标识需要创建使用专门的代理池的接口
 * @author qing
 */
public interface CreatePool {
    /**
     * 提供检测的url
     * @return String
     */
    String getCheckUrl();
}
