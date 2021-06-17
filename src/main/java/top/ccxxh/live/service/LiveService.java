package top.ccxxh.live.service;

import top.ccxxh.live.po.RoomInfo;

/**
 * @author qing
 */
public interface LiveService {
    /**
     * 获取直播房间信息
     * @param id 房间Id
     * @return JSONObject
     */
    RoomInfo getRoomInfo(Integer id);

    /**
     * 是否开播
     * @param id 房间Id
     * @return Boolean
     */
    Boolean getLiveStatus(Integer id);

    /**
     * 获取m3u8的地址
     * @param id 房间Id
     * @return String
     */
    String getM3u8Ulr(Integer id);

    /**
     * 获取flv的地址
     * @param id 房间Id
     * @return String
     */
    String getFlvUrl(Integer id);
}
