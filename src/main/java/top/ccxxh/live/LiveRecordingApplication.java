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
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.recording.AbsRecording;
import top.ccxxh.live.recording.FlvRecording;
import top.ccxxh.live.recording.M3u8Recording;
import top.ccxxh.live.service.LiveService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author qing
 */
@SpringBootApplication
public class LiveRecordingApplication implements CommandLineRunner {


    @Autowired
    @Qualifier("biliBiliServiceImpl")
    private LiveService biliBiliService;
    @Autowired
    @Qualifier("misServiceImpl")
    private LiveService misServiceImpl;


    @Autowired
    private LiveConfig liveConfig;

    @Autowired
    private LiveContent liveContent;

    public static void main(String[] args) {
        SpringApplication.run(LiveRecordingApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        liveContent.liveRecording(liveConfig.getBili(), biliBiliService);
        liveContent.liveRecording(liveConfig.getMis(), misServiceImpl);

    }


}
