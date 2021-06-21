package top.ccxxh.live.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ccxxh.live.LiveContent;
import top.ccxxh.live.po.RoomInfo;
import top.ccxxh.live.po.WebResult;
import top.ccxxh.live.recording.AbsRecording;

/**
 * @author qing
 */
@RestController
@RequestMapping("live")
public class LiveController {
    @Autowired
    private LiveContent liveContent;

    @GetMapping("stop/{id}/{source}")
    public WebResult<RoomInfo> setLiveStop(@PathVariable Integer id, @PathVariable String source) {
        final AbsRecording thread = liveContent.skip(source + "-" + id);
        return WebResult.succeed( thread.getRoomInfo());
    }
}
