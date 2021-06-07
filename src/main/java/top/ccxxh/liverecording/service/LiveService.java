package top.ccxxh.liverecording.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author qing
 */
public interface LiveService {
    /**
     * 获取直播房间信息
     * @param id 房间Id
     * @return JSONObject
     */
    JSONObject getRoomInfo(Integer id);

    /**
     * 获取直播连接
     * @param id 房间Id
     * @return String
     */
    String getLivePayUrl(Integer id);

    /**
     * 是否开播
     * @param id 房间Id
     * @return Boolean
     */
    Boolean getLiveStatus(Integer id);
}
