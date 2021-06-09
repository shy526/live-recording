package top.ccxxh.live.recording;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.autoconfigure.CloseExpiredConnectionsTask;
import top.ccxh.httpclient.tool.ThreadPoolUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author qing
 */
public abstract class AbsFlvRecording implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(AbsFlvRecording.class);

    public AbsFlvRecording(int roomId, long maxSize) {
        this.roomId = roomId;
        this.maxSize = maxSize;
    }

    /**
     * 房间号
     */
    private final Integer roomId;

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

    public String getNowPath() {
        return nowPath;
    }

    public void setNowPath(String nowPath) {
        this.nowPath = nowPath;
    }

    public Integer getRoomId() {
        return roomId;
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

    public boolean isStop() {
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
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadPoolUtils.getScheduledThreadPoolExecutor(roomId.toString(), 1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                this::sayHe, 10000, 10000, TimeUnit.MILLISECONDS);
        recording();
        stop();
        scheduledThreadPoolExecutor.shutdown();
    }

    /**
     * 录制核心
     */
    public abstract void recording();

    public void sayHe() {
        log.info("roomId:{},total:{},fileIndex:{}:stop:{}", roomId, total, fileIndex, stop);
        if (!stop) {

            log.info("{}:{}/{}-----{}%",roomId, nowPath, now,  new BigDecimal(now).divide(new BigDecimal(maxSize),2, BigDecimal.ROUND_HALF_UP));
        }
        log.info("roomId:{},recordingPaths:{}", roomId, pathList);
    }

}
