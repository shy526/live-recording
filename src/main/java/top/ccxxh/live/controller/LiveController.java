package top.ccxxh.live.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.ccxxh.live.LiveContent;
import top.ccxxh.live.constants.LiveSourceEnum;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.po.WebResult;
import top.ccxxh.live.recording.AbsRecording;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qing
 */
@RestController
@RequestMapping("live")
public class LiveController {
    @Autowired
    private LiveContent liveContent;

    @GetMapping("stop/{source}/{roomId}")
    public WebResult<RoomInfo> setLiveStop(@PathVariable Integer roomId, @PathVariable String source) {
        final AbsRecording thread = liveContent.skip(source + "-" + roomId);
        return WebResult.succeed(thread.getRoomInfo());
    }

    @GetMapping("recording/{source}")
    public void setLiveStop(@PathVariable String source, @RequestParam List<Integer> roomIds) {
        liveContent.liveRecording(roomIds, source);
    }

    @GetMapping("sources")
    public WebResult<List<String>> getSources() {
        LiveSourceEnum[] values = LiveSourceEnum.values();
        List<String> sources = new ArrayList<>(values.length);
        for (LiveSourceEnum item : values) {
            sources.add(item.getName());
        }
        return WebResult.succeed(sources);
    }
}
