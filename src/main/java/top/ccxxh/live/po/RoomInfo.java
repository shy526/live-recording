package top.ccxxh.live.po;

import java.io.Serializable;

/**
 * 房间信息
 *
 * @author qing
 */
public class RoomInfo implements Serializable {

    public RoomInfo( ) { }
    public RoomInfo(Integer roomId) {
        this.roomId = roomId;
    }
    public RoomInfo(Integer roomId,String source) {
        this.roomId = roomId;
        this.source = source;
    }

    private Integer uId;
    private Integer roomId;
    private String roomTitle;
    private String source;
    private String uName;

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
