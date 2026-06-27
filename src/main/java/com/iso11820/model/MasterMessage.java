package com.iso11820.model;

/**
 * 系统消息
 */
public class MasterMessage {
    private String time;     // 消息时间，格式 HH:mm:ss
    private String message;  // 消息内容
    private String type;     // 消息类型: normal, warning, error

    public MasterMessage() {}

    public MasterMessage(String time, String message) {
        this.time = time;
        this.message = message;
        this.type = "normal";
    }

    public MasterMessage(String time, String message, String type) {
        this.time = time;
        this.message = message;
        this.type = type;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}