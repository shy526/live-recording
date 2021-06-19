package top.ccxxh.live.recording;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.io.EofBufferedInputStream;
import top.ccxxh.live.po.M3u8;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.service.LiveService;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author qing
 */
public class M3u8Recording extends AbsFlvRecording {
    private final static Logger log = LoggerFactory.getLogger(M3u8Recording.class);
    private final static String SUFFIX = ".ts";

    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private final Set<String> read = new HashSet<>();

    public M3u8Recording(Integer roomId, LiveService liveService, HttpClientService httpClientService, long maxSize) {
        super(new RoomInfo(roomId), maxSize, liveService, httpClientService);
    }



    @Override
    public void recording() {
        before();
        boolean flag = false;
        String file = roomInfo.getuName() + DATA_FORMAT.format(new Date()) + "の%s" + "[" + getFileIndex() + "]" + SUFFIX;
        String tempPath = file + ".temp";
        setNowPath(tempPath);
        try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempPath))) {
            resetNow();
            while (true) {
                if (readM3u8PayList(fileOut)) {
                    break;
                }
                if (addNow(0)) {
                    flag = true;
                    break;
                }
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            read.clear();
        }
        after(flag, file, tempPath);

    }


    /**
     * 读取整个m3u8的PayList
     *
     * @param fileOut 输出地址
     * @return true m3u8文件无法获取 false 正常
     */
    private boolean readM3u8PayList(BufferedOutputStream fileOut) {
        String livePayUrl = liveService.getM3u8Ulr(roomInfo.getRoomId());
        String m3u8Str = httpClientService.get(livePayUrl).getEntityStr();
        if ("".equals(m3u8Str) || m3u8Str == null) {
            return true;
        }
        M3u8 m3u8 = M3u8.parse(livePayUrl, m3u8Str);
        for (String item : m3u8.getPayList()) {
            //过滤
            try {
                URL url = new URL(item);
                if (read.contains(url.getPath())) {
                    continue;
                } else {
                    read.add(url.getPath());
                }
            } catch (MalformedURLException e) {
                log.info(e.getMessage(), e);
            }

            try (
                    HttpResult httpResult = httpClientService.get(item);
                    InputStream liveIn = new EofBufferedInputStream(httpResult.getResponse().getEntity().getContent());
            ) {
                log.info(item);
                int len = -1;
                while ((len = liveIn.read(buff)) != -1 && !getStop()) {
                    addNow(len);
                    fileOut.write(buff, 0, len);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }


    private JSONObject getParams(String livePayUrl) {
        JSONObject result = new JSONObject();
        try {
            URL url = new URL(livePayUrl);
            String[] params = url.getQuery().split("&");
            for (String param : params) {
                String[] split = param.split("=");
                result.put(split[0], split[1]);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
