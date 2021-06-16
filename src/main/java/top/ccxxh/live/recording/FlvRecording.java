package top.ccxxh.live.recording;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.service.LiveService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qing
 */
public class FlvRecording extends AbsFlvRecording {
    private final static Logger log = LoggerFactory.getLogger(FlvRecording.class);
    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private final static SimpleDateFormat DATA_FORMAT_2 = new SimpleDateFormat("HH-mm-ss");
    private final static String SUFFIX = ".flv";
    private final byte[] buff = new byte[1024 * 4];
    private final static int MONITOR_TIME = 1000 * 20;

    public FlvRecording(Integer roomId, LiveService liveService, HttpClientService httpClientService, long maxSize) {
        super(new RoomInfo(roomId), maxSize, liveService, httpClientService);
    }


    @Override
    public void recording() {
        for (; !liveService.getLiveStatus(roomInfo.getRoomId())&&!getStop(); ) {
            log.info("{}:未开播", roomInfo.getuName());
            try {
                Thread.sleep(MONITOR_TIME);
            } catch (InterruptedException e) {
            }
        }
        boolean flag = false;
        addFileIndex();
        String file = roomInfo.getuName() + DATA_FORMAT.format(new Date()) + "の%s" + "[" + getFileIndex() + "]" + SUFFIX;
        String tempPath = file + ".temp";
        setNowPath(tempPath);
        String livePayUrl = liveService.getLivePayUrl(roomInfo.getRoomId());
        log.info("start:{}", livePayUrl);
        try (
                HttpResult httpResult = httpClientService.get(livePayUrl);
                InputStream liveIn = new BufferedInputStream(httpResult.getResponse().getEntity().getContent());
                BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempPath))
        ) {
            int len = -1;
            resetNow();
            while ((len = liveIn.read(buff)) != -1&&!getStop()) {
                fileOut.write(buff, 0, len);
                if (addNow(len)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        final File tempFile = new File(tempPath);
        if (tempFile.length() <= 0) {
            log.info("{}:delete", tempPath);
            tempFile.delete();
        } else {
            String path = String.format(file, DATA_FORMAT_2.format(new Date()));
            tempFile.renameTo(new File(path));
            log.info("{}:over", path);
            addPathList(path);
        }
        //网络异常或主播关播时 重置参数
        if (!flag) {
            resetFileIndex();
            log.info("{}:等待重新开播", roomInfo.getuName());
        }
        if (!getStop()){
            recording();
        }

    }
}
