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

/**
 * @author qing
 */
@Service
public class BiliBiliServiceImpl implements LiveService {
    private final static String ROOM_INFO_URL = "https://api.live.bilibili.com/room/v1/Room/room_init?id=%s";
    private final static String PAY_URL = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=%s&quality=3&platform=web";
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
        return null;
    }

    @Override
    public String getLivePayUrl(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(PAY_URL, id));
        JSONObject result = getJson(httpResult);
        JSONArray jsonArray = result.getJSONArray(KEY_D_URL);
        return jsonArray.getJSONObject(0).getString(KEY_URL);
    }

    @Override
    public Boolean getLiveStatus(Integer id) {
        HttpResult httpResult = httpClientService.get(String.format(ROOM_INFO_URL, id));
        JSONObject result = getJson(httpResult);
        log.info(result.toJSONString());
        return  result.getIntValue(KEY_LIVE_STATUS)==LIVE;
    }

    private JSONObject getJson(HttpResult httpResult) {
        String entityStr = httpResult.getEntityStr();
        JSONObject result = JSON.parseObject(entityStr);
        return result.getJSONObject(KEY_DATA);
    }


}
