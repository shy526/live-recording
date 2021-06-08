package top.ccxxh.live.recording;

import java.util.ArrayList;
import java.util.List;

public class TaskInfo {

    public TaskInfo(int roomId, long maxSize) {
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


    public void addNow(long len) {
        now += len;
        total += len;
    }

    public void addPathList(String path) {
        pathList.add(path);
    }

    public void stop() {
        stop = true;
    }

}
