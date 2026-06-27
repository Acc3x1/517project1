package com.iso11820.util;

/**
 * 温度计算工具类
 */
public class TemperatureCalculator {

    /**
     * 计算温升
     */
    public static double calculateDeltaT(double finalTemp, double initialTemp) {
        return finalTemp - initialTemp;
    }

    /**
     * 计算失重率
     */
    public static double calculateLostWeightPer(double preWeight, double postWeight) {
        if (preWeight == 0) return 0;
        return ((preWeight - postWeight) / preWeight) * 100;
    }

    /**
     * 计算平均温度
     */
    public static double calculateAverage(double[] temps) {
        if (temps == null || temps.length == 0) return 0;
        double sum = 0;
        for (double t : temps) {
            sum += t;
        }
        return sum / temps.length;
    }

    /**
     * 计算温度偏差
     */
    public static double calculateDeviation(double temp, double target) {
        return Math.abs(temp - target);
    }

    /**
     * 检查温度是否在范围内
     */
    public static boolean isInRange(double temp, double min, double max) {
        return temp >= min && temp <= max;
    }

    /**
     * 计算温漂 (线性回归斜率)
     */
    public static double calculateDrift(double[] temps) {
        if (temps == null || temps.length < 2) return 0;

        int n = temps.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += temps[i];
            sumXY += i * temps[i];
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope * 600; // 转换为每10分钟的温漂
    }
}