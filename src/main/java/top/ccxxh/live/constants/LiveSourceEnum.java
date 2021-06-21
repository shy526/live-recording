package top.ccxxh.live.constants;

/**
 * 支持的直播源类型
 * @author qing
 */

public enum LiveSourceEnum {
    /**
     * b站
     */
    BILI_BILI("bilibili",1),
    /**
     * 猫耳
     */
    MISS_EVAN("missevan",2),
    ;
    private final String name;
    private final Integer code;

    LiveSourceEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

}
