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

    private final static long MAX_SIZE = (long) ((1000L * 1000L) * 400D);

    @Override
    public void run(String... args) throws Exception {
        liveRecording(liveConfig.getBili(), biliBiliService);
        liveRecording(liveConfig.getMis(), misServiceImpl);
    }

    private void liveRecording(List<Integer> list, LiveService liveService) {
        if (list == null || list.isEmpty()) {
            log.info("no roomId lsit");
            return;
        }
        for (Integer item : list) {
            final FlvRecording flvRecording = new FlvRecording(item, liveService, httpClientService, MAX_SIZE);
            new Thread(flvRecording).start();
        }
    }
}
