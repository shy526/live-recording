package top.ccxxh.live.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.service.LiveService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qing
 */
@Service
public class BiliBiliServiceImpl implements LiveService {
    private final static String ROOM_INFO_URL = "https://api.live.bilibili.com/room/v1/Room/room_init?id=%s";
    private final static String PAY_URL = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=%s&quality=3&platform=web";
    private final static String PAY_URL_2 = "https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo?room_id=%s&protocol=0,1&format=0,1,2&codec=0,1&qn=150&platform=web&ptype=8";
    private final static String ROOM_INFO_URL_2 = "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=%s";
    private final static String KEY_DATA = "data";
    private final static String KEY_LIVE_STATUS = "live_status";
    private final static String KEY_D_URL = "durl";
    private final static String KEY_URL = "url";
    private final static int LIVE = 1;
    private final static Logger log = LoggerFactory.getLogger(BiliBiliServiceImpl.class);
    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public JSONObject getRoomInfo(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL_2, id));
        return getJson(httpResult);
    }

    @Override
    public String getLivePayUrl(Integer id) {
/*        HttpResult httpResult = httpClientService.get(String.format(PAY_URL, id));
        JSONObject result = getJson(httpResult);
        JSONArray jsonArray = result.getJSONArray(KEY_D_URL);
        return jsonArray.getJSONObject(0).getString(KEY_URL);*/
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.live.bilibili.com");
        HttpResult httpResult = httpClientService.get(String.format(PAY_URL_2,id), null, header);
        return test(httpResult);
    }

    @Override
    public Boolean getLiveStatus(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        JSONObject result = getJson(httpResult);
        log.info(result.toJSONString());
        return result.getIntValue(KEY_LIVE_STATUS) == LIVE;
    }

    private JSONObject getJson(HttpResult httpResult) {
        String entityStr = httpResult.getEntityStr();
        JSONObject result = JSON.parseObject(entityStr);
        return result.getJSONObject(KEY_DATA);
    }

    private String test(HttpResult httpResult) {
        final JSONObject data = getJson(httpResult);
        final JSONArray stream = data.getJSONObject("playurl_info").getJSONObject("playurl").getJSONArray("stream");
        final JSONObject item = stream.getJSONObject(0);
        final JSONObject format = item.getJSONArray("format").getJSONObject(0);
        final JSONObject codec = format.getJSONArray("codec").getJSONObject(0);
        final String base_url = codec.getString("base_url");
        final JSONObject urlInfo = codec.getJSONArray("url_info").getJSONObject(0);
        return urlInfo.getString("host") + base_url + urlInfo.getString("extra");

    }

}
