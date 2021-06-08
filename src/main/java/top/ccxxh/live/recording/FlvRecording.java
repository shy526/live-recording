package top.ccxxh.live.recording;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.service.LiveService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author qing
 */
public class FlvRecording implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(FlvRecording.class);
    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private final static String SUFFIX = ".flv";

    public FlvRecording(Integer roomId, LiveService liveService, HttpClientService httpClientService, long maxSize) {
        this.roomId = roomId;
        this.liveService = liveService;
        this.httpClientService = httpClientService;
        this.maxSize = maxSize;
    }

    /**
     * 房间号
     */
    private final Integer roomId;
    /**
     * 处理live的服务
     */
    private final LiveService liveService;
    private final HttpClientService httpClientService;
    /**
     * 文件最大容量
     */
    private final long maxSize;
    private final byte[] buff = new byte[1024 * 4];
    /**
     * 分p文件
     */
    private int fileIndex = 0;
    /**
     * 所有已经生成的文件路径
     */
    private final List<String> pathList = new ArrayList<>();
    /**
     * 总的byte数
     */
    private long total = 0;
    /**
     * 当前文件的byte数
     */
    private long now = 0;
    private boolean stop = false;

    @Override
    public void run() {
        if (!liveService.getLiveStatus(roomId)) {
            stop = true;
            return;
        }

        log.info("start");
        recording();
        stop = true;
        log.info("end");
    }

    private void recording() {
        boolean flag = false;
        fileIndex++;
        String file = roomId + "=" + DATA_FORMAT.format(new Date()) + "の%s" + "[" + fileIndex + "]" + SUFFIX;
        String tempFile = file + ".temp";
        String livePayUrl = liveService.getLivePayUrl(roomId);
        log.info("start:{}", livePayUrl);
        HttpResult httpResult = httpClientService.get(livePayUrl);

        try (
                BufferedInputStream liveIn = new BufferedInputStream(httpResult.getResponse().getEntity().getContent());
                BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempFile))
        ) {
            int len = -1;
            now = 0;
            while ((len = liveIn.read(buff)) != -1) {
                fileOut.write(buff, 0, len);
                now += len;
                total += len;
                if (now >= maxSize) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String path = String.format(file, DATA_FORMAT.format(new Date()));
        new File(tempFile).renameTo(new File(path));
        pathList.add(path);

        if (flag) {
            recording();
        }
    }

    public JSONObject info() {
        JSONObject info = new JSONObject();
        info.put("roomId", roomId);
        info.put("fileIndex", fileIndex);
        info.put("pathList", pathList);
        info.put("total", total);
        info.put("now", now);
        info.put("maxSize", maxSize);
        info.put("stop", stop);
        return info;
    }
}
