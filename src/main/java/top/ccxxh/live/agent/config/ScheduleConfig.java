package top.ccxxh.live.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import top.ccxh.httpclient.tool.ThreadPoolUtils;

/**
 * 异步定时任务配置
 *
 * @author qing
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(ThreadPoolUtils.getScheduledThreadPoolExecutor("schedule-take",5));
    }

}