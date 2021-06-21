package top.ccxxh.live.service;

import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.recording.AbsRecording;

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

    /**
     * 指定源
     * @return 指定源
     */
    String getSource();

    /**
     * 指定录制方式
     * @return Class<? extends AbsRecording>
     */
    Class<? extends AbsRecording> getRecording();

    /**
     * 分片大小
     * @return long
     */
    long getSplitSize();
}
