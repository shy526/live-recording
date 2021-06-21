package top.ccxxh.live.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.recording.AbsRecording;
import top.ccxxh.live.recording.M3u8Recording;
import top.ccxxh.live.service.LiveService;

/**
 * 猫耳
 *
 * @author qing
 */
@Service
public class MisServiceImpl implements LiveService {
    private final static String ROOM_INFO_URL = "https://fm.missevan.com/api/v2/live/%s";
    private final static Logger log = LoggerFactory.getLogger(BiliBiliServiceImpl.class);
    @SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @Autowired
    private HttpClientService httpClientService;
    private final static int LIVE = 1;

    @Override
    public RoomInfo getRoomInfo(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        return liveRoomInfo2RoomInfo(httpResult);
    }

    @Override
    public Boolean getLiveStatus(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        JSONObject json = getJson(httpResult);
        return json.getJSONObject("room").getJSONObject("status").getInteger("open").equals(LIVE);
    }

    @Override
    public String getM3u8Ulr(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        JSONObject json = getJson(httpResult);
        return json.getJSONObject("room").getJSONObject("channel").getString("hls_pull_url");
    }

    @Override
    public String getFlvUrl(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        JSONObject json = getJson(httpResult);
        return json.getJSONObject("room").getJSONObject("channel").getString("flv_pull_url");
    }

    @Override
    public String getSource() {
        return "mis";
    }

    @Override
    public Class<? extends AbsRecording> getRecording() {
        return M3u8Recording.class;
    }

    @Override
    public long getSplitSize() {
        return (long) ((1000L * 1000L) * 40D);
    }


    private JSONObject getJson(HttpResult httpResult) {
        String entityStr = httpResult.getEntityStr();
        JSONObject result = JSON.parseObject(entityStr);

        return result != null ? result.getJSONObject("info") : null;
    }


    private RoomInfo liveRoomInfo2RoomInfo(HttpResult httpResult) {
        JSONObject json = getJson(httpResult);
        RoomInfo result = new RoomInfo();
        JSONObject roomInfo = json.getJSONObject("room");
        JSONObject anchorInfo = json.getJSONObject("creator");
        result.setRoomTitle(roomInfo.getString("name"));
        result.setRoomId(roomInfo.getInteger("room_id"));
        result.setSource(getSource());
        result.setuId(roomInfo.getInteger("creator_id"));
        result.setuName(anchorInfo.getString("username"));
        return result;
    }
}
