package com.iso11820.model;

/**
 * 温度数据点 - 用于曲线图和CSV记录
 */
public class TemperatureData {
    private double time;      // 时间(秒) — 800ms一个tick，转换为实际秒数
    private double tf1;        // 炉温1
    private double tf2;        // 炉温2
    private double ts;         // 表面温度
    private double tc;         // 中心温度
    private double tCal;       // 校准温度

    public TemperatureData() {}

    public TemperatureData(double time, double tf1, double tf2, double ts, double tc, double tCal) {
        this.time = time;
        this.tf1 = tf1;
        this.tf2 = tf2;
        this.ts = ts;
        this.tc = tc;
        this.tCal = tCal;
    }

    // Getters and Setters
    public double getTime() { return time; }
    public void setTime(double time) { this.time = time; }

    public double getTf1() { return tf1; }
    public void setTf1(double tf1) { this.tf1 = tf1; }

    public double getTf2() { return tf2; }
    public void setTf2(double tf2) { this.tf2 = tf2; }

    public double getTs() { return ts; }
    public void setTs(double ts) { this.ts = ts; }

    public double getTc() { return tc; }
    public void setTc(double tc) { this.tc = tc; }

    public double getTCal() { return tCal; }
    public void setTCal(double tCal) { this.tCal = tCal; }

    /**
     * 转换为CSV行
     */
    public String toCsvLine() {
        return String.format("%.1f,%.1f,%.1f,%.1f,%.1f,%.1f", time, tf1, tf2, ts, tc, tCal);
    }
}