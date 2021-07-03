package top.ccxxh.live;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.ccxxh.live.agent.AgentManager;
import top.ccxxh.live.agent.spider.Ip66Spider;
import top.ccxxh.live.config.LiveConfig;
import top.ccxxh.live.constants.LiveSourceEnum;
import top.ccxxh.live.service.LiveService;

import java.util.List;
import java.util.Map;

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
    private LiveContent liveContent;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private LiveConfig liveConfig;

    public static void main(String[] args) {
        SpringApplication.run(LiveRecordingApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        Map<String, LiveService> liveServiceMap = applicationContext.getBeansOfType(LiveService.class);
        Map<String, List<Integer>> roomIdGroup = liveConfig.getRoomIdGroup();
        liveServiceMap.entrySet().forEach(item -> {
            LiveService service = item.getValue();
            LiveSourceEnum source = service.getSource();
            List<Integer> roomIds = roomIdGroup.get(source.getName());
            liveContent.liveRecording(roomIds,service);
        });
    }


}
