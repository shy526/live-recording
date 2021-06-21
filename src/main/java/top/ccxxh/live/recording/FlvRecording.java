package top.ccxxh.live.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.io.EofBufferedInputStream;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.service.LiveService;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qing
 */
public class FlvRecording extends AbsRecording {
    private final static Logger log = LoggerFactory.getLogger(FlvRecording.class);
    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    private final static String SUFFIX = ".flv";


    public FlvRecording(Integer roomId, LiveService liveService, HttpClientService httpClientService, long maxSize) {
        super(new RoomInfo(roomId, liveService.getSource()), maxSize, liveService, httpClientService);
    }


    @Override
    public void recording() {
        before();
        boolean flag = false;
        String file = roomInfo.getuName() + DATA_FORMAT.format(new Date()) + "の%s" + "[" + getFileIndex() + "]" + SUFFIX;
        String tempPath = file + ".temp";
        setNowPath(tempPath);
        String livePayUrl = liveService.getFlvUrl(roomInfo.getRoomId());
        log.info("start:{}", livePayUrl);
        try (
                HttpResult httpResult = httpClientService.get(livePayUrl);
                InputStream liveIn = new EofBufferedInputStream(httpResult.getResponse().getEntity().getContent());
                BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempPath))
        ) {
            int len = -1;
            resetNow();
            while ((len = liveIn.read(buff)) != -1 && !getSkip()) {
                fileOut.write(buff, 0, len);
                if (addNow(len)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        after(flag, file, tempPath);

    }


}
