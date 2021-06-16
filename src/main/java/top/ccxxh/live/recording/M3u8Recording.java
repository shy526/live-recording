package top.ccxxh.live.recording;

import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.service.LiveService;

/**
 *
 * @author qing
 */
public class M3u8Recording extends AbsFlvRecording {

    public M3u8Recording(Integer roomId, LiveService liveService, HttpClientService httpClientService, long maxSize) {
        super(new RoomInfo(roomId), maxSize, liveService, httpClientService);
    }

    @Override
    public void recording() {

    }
}
