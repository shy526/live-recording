package top.ccxxh.live.recording;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxh.httpclient.tool.ThreadPoolUtils;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.service.LiveService;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author qing
 */
public abstract class AbsFlvRecording implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(AbsFlvRecording.class);
    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("HH-mm-ss");

    public AbsFlvRecording(RoomInfo roomInfo,Long maxSize, LiveService liveService, HttpClientService httpClientService) {
        this.maxSize = maxSize;
        this.liveService = liveService;
        this.roomInfo = roomInfo;
        this.httpClientService = httpClientService;
    }



    /**
     * 文件最大容量
     */
    private final Long maxSize;

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
    private String nowPath;

    /**
     * 处理live的服务
     */
    protected final LiveService liveService;
    protected final HttpClientService httpClientService;
    private final static int MONITOR_TIME = 1000 * 5;
    /**
     * 房间信息
     */
    protected  RoomInfo roomInfo;

    protected final byte[] buff = new byte[1024 * 4];

    public String getNowPath() {
        return nowPath;
    }

    public void setNowPath(String nowPath) {
        this.nowPath = nowPath;
    }


    public long getMaxSize() {
        return maxSize;
    }


    public int getFileIndex() {
        return fileIndex;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public long getTotal() {
        return total;
    }

    public long getNow() {
        return now;
    }

    public boolean getStop() {
        return stop;
    }


    public boolean addNow(long len) {
        now += len;
        total += len;
        return now >= maxSize;
    }

    public void resetNow() {
        now = 0;
    }
    public void resetFileIndex() {
        fileIndex = 0;
    }

    public void addFileIndex() {
        fileIndex++;
    }

    public void addPathList(String path) {
        pathList.add(path);
    }

    public void stop() {
        stop = true;
        sayHe();
    }

    @Override
    public void run() {
         RoomInfo roomInfo = liveService.getRoomInfo(this.roomInfo.getRoomId());
         this.roomInfo=roomInfo!=null ?roomInfo:this.roomInfo;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadPoolUtils.getScheduledThreadPoolExecutor(roomInfo.getRoomId().toString(), 1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                this::sayHe, 10000, 30000, TimeUnit.MILLISECONDS);
        recording();
        scheduledThreadPoolExecutor.shutdown();
        sayHe();
    }

    /**
     * 录制核心
     */
    public abstract void recording();

    public void sayHe() {
        Integer roomId = roomInfo.getRoomId();
        String jsonStr= JSON.toJSONString(roomInfo);
        String infoStr = "\n" +
                "-----------------{}------------------- \n" +
                "roomId:{},total:{},fileIndex:{}:stop:{} \n";
        boolean stopFlag = stop;
        infoStr += stop ? "" : "{}:{}/{}-----{} \n";
        boolean emptyFlag = pathList.isEmpty();
        infoStr += emptyFlag ? "" : "recordingPaths:{} \n";
        infoStr +=  "-----------------{}------------------- \n";
        if (!stopFlag && !emptyFlag) {
            log.info(infoStr,jsonStr,
                    roomId, total, fileIndex, stop,
                    nowPath, now, maxSize, new BigDecimal(now).divide(new BigDecimal(maxSize), 2, BigDecimal.ROUND_HALF_UP)
                    , pathList,
                    jsonStr);
            return;
        }
        if (!stopFlag) {
            log.info(infoStr,jsonStr,
                    roomId, total, fileIndex, stop,
                    nowPath, now, maxSize, new BigDecimal(now).divide(new BigDecimal(maxSize), 2, BigDecimal.ROUND_HALF_UP),
                    jsonStr);
            return;
        }
        if (!emptyFlag) {
            log.info(infoStr,jsonStr, roomId, total, fileIndex, stop,
                    pathList,
                    jsonStr);
        }

    }

    protected void reNameTo(String path,String targetPath){
        final File tempFile = new File(path);
        if (tempFile.length() <= 0) {
            log.info("{}:delete", path);
            tempFile.delete();
        } else {
            String newTargetPath = String.format(targetPath, DATA_FORMAT.format(new Date()));
            tempFile.renameTo(new File(newTargetPath));
            log.info("{}:over", newTargetPath);
            addPathList(newTargetPath);
        }

    }

    protected void after(boolean flag, String file, String tempPath) {
        reNameTo(tempPath, file);
        //网络异常或主播关播时 重置参数
        if (!flag) {
            resetFileIndex();
            log.info("{}:等待重新开播", roomInfo.getuName());
        }
        if (!getStop()){
            recording();
        }
    }

    protected void before() {
        for (; !liveService.getLiveStatus(roomInfo.getRoomId()) && !getStop(); ) {
            log.info("{}:未开播", roomInfo.getuName());
            try {
                Thread.sleep(MONITOR_TIME);
            } catch (InterruptedException e) {
            }
        }
        addFileIndex();
    }

}
