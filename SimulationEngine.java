package com.iso11820.service;

import com.iso11820.config.AppConfig.SimulationConfig;
import com.iso11820.model.SimulatedPhenomenon;
import com.iso11820.model.TemperatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 温度仿真引擎 - 生成5通道温度数据
 */
public class SimulationEngine {
    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);

    // 配置参数
    private final SimulationConfig config;
    private final Random random = new Random();

    // 当前温度值
    private double tf1;    // 炉温1
    private double tf2;    // 炉温2
    private double ts;     // 表面温度
    private double tc;     // 中心温度
    private double tCal;   // 校准温度

    // 状态标志
    private boolean isRecording = false;
    private boolean isHeating = false;
    private int stableCount = 0;

    // 目标温度
    private double targetTemp;
    private double stableThreshold;
    private double heatingRate;
    private double fluctuation;

    public SimulationEngine(SimulationConfig config) {
        this.config = config;
        this.targetTemp = config.getTargetFurnaceTemp();
        this.stableThreshold = config.getStableThreshold();
        this.heatingRate = config.getHeatingRatePerSecond();
        this.fluctuation = config.getTempFluctuation();

        // 初始化温度
        reset();
    }

    /**
     * 重置温度到初始状态
     */
    public void reset() {
        tf1 = config.getInitialFurnaceTemp();
        tf2 = config.getInitialFurnaceTemp();
        ts = tf1 * 0.3;
        tc = tf1 * 0.25;
        tCal = tf1;
        stableCount = 0;
        isRecording = false;
        isHeating = false;
        logger.info("仿真引擎重置，初始温度: {}", tf1);
    }

    /**
     * 开始升温
     */
    public void startHeating() {
        isHeating = true;
        stableCount = 0;
        logger.info("开始升温");
    }

    /**
     * 停止升温
     */
    public void stopHeating() {
        isHeating = false;
        logger.info("停止升温");
    }

    /**
     * 开始记录
     */
    public void startRecording() {
        isRecording = true;
        logger.info("开始记录温度数据");
    }

    /**
     * 停止记录
     */
    public void stopRecording() {
        isRecording = false;
        logger.info("停止记录");
    }

    /**
     * 更新温度 - 每800ms调用一次
     * @param currentTimeSec 当前时间（秒，由DaqWorker转换为实际秒数）
     * @return 温度数据对象
     */
    public TemperatureData update(double currentTimeSec) {
        // 生成随机噪声 [-1, 1] * fluctuation
        double noise = (random.nextDouble() * 2 - 1) * fluctuation;
        double noise2 = (random.nextDouble() * 2 - 1) * fluctuation;
        double noiseSmall = (random.nextDouble() * 2 - 1) * fluctuation * 0.5;

        if (isHeating) {
            // 升温阶段：温度向目标温度靠近
            if (tf1 < targetTemp - stableThreshold) {
                // 未达到稳定区域，继续升温
                double increment = heatingRate * 0.8; // 800ms = 0.8s
                tf1 += increment + noise;
                tf2 += increment + noise2;

                // 非记录阶段，样品温度低值跟随
                ts = tf1 * 0.3 + noiseSmall;
                tc = tf1 * 0.25 + noiseSmall;
            } else {
                // 达到稳定区域，钳位到目标温度
                tf1 = targetTemp + noise;
                tf2 = targetTemp + noise2;

                // 稳定计数器增加
                stableCount++;
            }

            tCal = tf1 + noise * 2;
        }

        if (isRecording) {
            // 记录阶段：样品温度指数上升
            double surfaceTarget = Math.min(tf1 * 0.95, 800);
            ts += (surfaceTarget - ts) * 0.02 + noiseSmall;

            double centerTarget = Math.min(tf1 * 0.85, 750);
            tc += (centerTarget - tc) * 0.01 + noiseSmall;

            // 炉温保持在目标温度
            tf1 = targetTemp + noise;
            tf2 = targetTemp + noise2;
            tCal = tf1 + noise * 2;
        }

        if (!isHeating && !isRecording) {
            // 降温阶段
            tf1 -= 0.5 + noiseSmall * 0.1;
            tf2 -= 0.5 + noiseSmall * 0.1;
            if (tf1 < 25) tf1 = 25;
            if (tf2 < 25) tf2 = 25;
            ts = tf1 * 0.3;
            tc = tf1 * 0.25;
            tCal = tf1;
        }

        return new TemperatureData(currentTimeSec, tf1, tf2, ts, tc, tCal);
    }

    /**
     * 检查温度是否稳定
     */
    public boolean isStable() {
        return stableCount > 3; // 连续3次以上(约3.2秒)稳定
    }

    /**
     * 检查温度是否在目标范围内
     */
    public boolean isInTargetRange() {
        return tf1 >= targetTemp - 5 && tf1 <= targetTemp + 5; // 745~755°C
    }

    /**
     * 检查是否可以开始记录
     */
    public boolean canStartRecording() {
        return isStable() && isInTargetRange();
    }

    // Getters
    public double getTf1() { return tf1; }
    public double getTf2() { return tf2; }
    public double getTs() { return ts; }
    public double getTc() { return tc; }
    public double getTCal() { return tCal; }
    public boolean isHeating() { return isHeating; }
    public boolean isRecording() { return isRecording; }

    // 运行时参数更新方法（由参数设置界面调用）
    public void setTargetTemp(double targetTemp) { this.targetTemp = targetTemp; }
    public void setHeatingRate(double heatingRate) { this.heatingRate = heatingRate; }
    public void setFluctuation(double fluctuation) { this.fluctuation = fluctuation; }
    public void setStableThreshold(double stableThreshold) { this.stableThreshold = stableThreshold; }

    /**
     * 仿真生成试验现象数据 — 模拟真实实验中操作员观察到的结果
     *
     * ISO 11820 不燃性判定标准（简化版）：
     *   - 失重率 ≤ 50%   合格
     *   - 火焰持续时间 < 5秒  合格
     *
     * 仿真逻辑：
     *   - 70% 概率模拟"合格（不燃材料）"：失重很小(1%~8%)，无火焰或仅有短暂火焰
     *   - 30% 概率模拟"不合格（可燃材料）"：失重大(30%~60%)，必有持续火焰
     *
     * @param preWeight 试验前质量(g)
     * @param testDuration 试验总时长(秒)，用于限定火焰发生时刻范围
     * @return 仿真现象数据
     */
    public SimulatedPhenomenon simulatePhenomenon(double preWeight, int testDuration) {
        // 70% 概率判定为合格（不燃材料演示为主）
        boolean isPassing = random.nextDouble() < 0.7;

        int safeDuration = testDuration > 0 ? testDuration : 3600;

        if (isPassing) {
            // 合格：不燃材料 — 失重很小（1%~8%）
            double lossRate = 0.01 + random.nextDouble() * 0.07; // 1%~8%
            double postWeight = preWeight * (1 - lossRate);

            // 80% 无火焰；20% 有短暂火焰（<5秒，仍合格）
            if (random.nextDouble() < 0.2) {
                int flameTime = random.nextInt(Math.max(safeDuration / 4, 1)) + 1;
                int flameDuration = 1 + random.nextInt(4); // 1~4秒
                return new SimulatedPhenomenon(postWeight, true, flameTime, flameDuration, true);
            } else {
                return new SimulatedPhenomenon(postWeight, false, 0, 0, true);
            }
        } else {
            // 不合格：可燃材料 — 失重大（30%~60%）
            double lossRate = 0.30 + random.nextDouble() * 0.30; // 30%~60%
            double postWeight = preWeight * (1 - lossRate);

            // 必有持续火焰（≥5秒）
            int flameTime = random.nextInt(Math.max(safeDuration / 2, 1)) + 1;
            int flameDuration = 5 + random.nextInt(20); // 5~24秒
            return new SimulatedPhenomenon(postWeight, true, flameTime, flameDuration, false);
        }
    }
}