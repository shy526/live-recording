package top.ccxxh.live;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.config.LiveConfig;
import top.ccxxh.live.recording.FlvRecording;
import top.ccxxh.live.recording.M3u8Recording;
import top.ccxxh.live.service.LiveService;

import java.util.List;

/**
 * @author qing
 */
@SpringBootApplication
public class LiveRecordingApplication implements CommandLineRunner {
    private final static Logger log = LoggerFactory.getLogger(LiveRecordingApplication.class);
    @Autowired
    @Qualifier("biliBiliServiceImpl")
    private LiveService biliBiliService;
    @Autowired
    @Qualifier("misServiceImpl")
    private LiveService misServiceImpl;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private LiveConfig liveConfig;

    public static void main(String[] args) {
        SpringApplication.run(LiveRecordingApplication.class, args);
    }

    private final static long VIDEO_MAX_SIZE = (long) ((1000L * 1000L) * 400D);
    private final static long AUDIO_MAX_SIZE = (long) ((1000L * 1000L) * 4D);

    @Override
    public void run(String... args) throws Exception {
        flvLiveRecording(liveConfig.getBili(), biliBiliService,VIDEO_MAX_SIZE);
        m3u8LiveRecording(liveConfig.getMis(), misServiceImpl,AUDIO_MAX_SIZE);
    }

    private void flvLiveRecording(List<Integer> list, LiveService liveService,long size) {
        if (list == null || list.isEmpty()) {
            log.info("no roomId list");
            return;
        }
        for (Integer item : list) {
            final FlvRecording flvRecording = new FlvRecording(item, liveService, httpClientService, size);
            new Thread(flvRecording).start();
        }
    }

    private void m3u8LiveRecording(List<Integer> list, LiveService liveService,long size) {
        if (list == null || list.isEmpty()) {
            log.info("no roomId list");
            return;
        }
        for (Integer item : list) {
            final M3u8Recording m3u8Recording = new M3u8Recording(item, liveService, httpClientService, size);
            new Thread(m3u8Recording).start();
        }
    }
}
