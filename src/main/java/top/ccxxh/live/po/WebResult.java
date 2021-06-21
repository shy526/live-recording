package top.ccxxh.live.po;

import java.io.Serializable;

/**
 * @author qing
 */
public class WebResult<T> implements Serializable {
    private String message;
    private Integer code;
    private T data;
    /**
     * 成功
     */
    private final static int SUCCEED = 200;
    /**
     * 失败
     */
    private final static int FAILURE = 400;
    /**
     * 重定向
     */
    private final static int REDIRECT = 302;

    /**
     * 执行成功
     *
     * @param data 数据
     * @param <T>  泛型
     * @return WebResult<T>
     */
    public static <T> WebResult<T> succeed(T data) {
        return buildWebResult(null, data, SUCCEED);
    }

    /**
     * 执行成功
     *
     * @param <T> 泛型
     * @return WebResult<T>
     */
    public static <T> WebResult<T> succeed() {
        return buildWebResult(null, null, SUCCEED);
    }

    /**
     * 执行成功
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     泛型
     * @return WebResult<T>
     */
    public static <T> WebResult<T> succeed(String message, T data) {
        return buildWebResult(message, data, SUCCEED);
    }


    /**
     * 执行错误
     *
     * @param message 错误信息
     * @param <T>     泛型
     * @return WebResult<T>
     */
    public static <T> WebResult<T> failure(String message) {
        return buildWebResult(message, null, FAILURE);

    }

    /**
     * 重定向
     *
     * @param data 重定向url
     * @return WebResult<String>
     */
    public static WebResult<String> redirect(String data) {
        return buildWebResult(null, data, REDIRECT);
    }

    /**
     * 重定向
     *
     * @param message 反馈信息
     * @param data    数据
     * @return WebResult<String>
     */
    public static <T> WebResult<String> redirect(String message, String data) {
        return buildWebResult(message, data, REDIRECT);
    }

    /**
     * 建立 WebResult 基本方法
     *
     * @param message 消息
     * @param data    数据
     * @param code    返回code
     * @param <T>     泛型
     * @return WebResult<T>
     */
    public static <T> WebResult<T> buildWebResult(String message, T data, int code) {
        WebResult<T> webResult = new WebResult<>();
        webResult.setCode(code);
        webResult.setMessage(message);
        webResult.setData(data);

        return webResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}