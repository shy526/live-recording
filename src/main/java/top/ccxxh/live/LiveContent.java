package top.ccxxh.live;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.recording.AbsRecording;
import top.ccxxh.live.service.LiveService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播上下文
 *
 * @author qing
 */
@Component
public class LiveContent {
    private final Map<String, AbsRecording> liveRunThread = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(LiveRecordingApplication.class);

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private ApplicationContext applicationContext;

    public AbsRecording skip(String key) {
        AbsRecording absRecording = liveRunThread.get(key);
        absRecording.skip();
        liveRunThread.remove(key);
        return absRecording;
    }

    public void addRunThread(String key, AbsRecording thread) {
        liveRunThread.put(key, thread);
    }


    public void liveRecording(List<Integer> list, LiveService liveService) {
        if (list == null || list.isEmpty()) {
            log.info("no roomId list");
            return;
        }
        for (Integer item : list) {
            Constructor<?>[] constructors = liveService.getRecording().getConstructors();
            try {
                AbsRecording thread = (AbsRecording) constructors[0].newInstance(item, liveService, httpClientService, liveService.getSplitSize());
                final RoomInfo roomInfo = thread.getRoomInfo();
                addRunThread(roomInfo.getSource().getName() + "-" + roomInfo.getRoomId(), thread);
                thread.start();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void liveRecording(List<Integer> list, String source) {
        Map<String, LiveService> beans = applicationContext.getBeansOfType(LiveService.class);
        for (Map.Entry<String, LiveService> item : beans.entrySet()) {
            LiveService value = item.getValue();
            if (value.getSource().equals(source)) {
                liveRecording(list, value);
                break;
            }
        }
    }
}
