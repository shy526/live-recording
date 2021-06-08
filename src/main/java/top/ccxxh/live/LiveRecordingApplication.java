package top.ccxxh.live;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.live.service.LiveService;
import top.ccxxh.live.recording.FlvRecording;

/**
 * @author qing
 */
@SpringBootApplication
public class LiveRecordingApplication implements CommandLineRunner {
    private final static Logger log = LoggerFactory.getLogger(LiveRecordingApplication.class);
    @Autowired
    private LiveService biliBiliService;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired
    private HttpClientService httpClientService;

    public static void main(String[] args) {
        SpringApplication.run(LiveRecordingApplication.class, args);
    }

    private final static long MAX_SIZE = (long) ((1000L * 1000L) * 2D);

    @Override
    public void run(String... args) throws Exception {
        FlvRecording flvRecording = new FlvRecording(22621440, biliBiliService, httpClientService, MAX_SIZE);
        new Thread(flvRecording).start();

/*        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(10000);
                    if (flvRecording != null) {
                        log.info(flvRecording.info().toJSONString());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
        log.info("_______________________________________");
    }
}
