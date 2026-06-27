package com.iso11820.service;

import com.iso11820.GlobalContext;
import com.iso11820.controller.StateMachine;
import com.iso11820.controller.TestController;
import com.iso11820.model.TemperatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 数据采集服务 - 每800ms定时运行
 */
public class DaqWorker {
    private static final Logger logger = LoggerFactory.getLogger(DaqWorker.class);

    private final GlobalContext context;
    private final int intervalMs;
    private ScheduledExecutorService scheduler;
    private boolean running = false;

    // 当前时间计数(秒)
    private int currentTime = 0;

    // 数据更新回调
    private Consumer<TemperatureData> onDataUpdate;

    public DaqWorker(GlobalContext context, int intervalMs) {
        this.context = context;
        this.intervalMs = intervalMs;
    }

    /**
     * 启动数据采集
     */
    public void start() {
        if (running) return;

        running = true;
        currentTime = 0;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                doWork();
            } catch (Exception e) {
                logger.error("数据采集异常", e);
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        logger.info("数据采集服务启动，间隔 {} ms", intervalMs);
    }

    /**
     * 停止数据采集
     */
    public void stop() {
        if (!running) return;

        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        logger.info("数据采集服务停止");
    }

    /**
     * 数据采集工作 - 每800ms执行
     */
    private void doWork() {
        SimulationEngine engine = context.getSimulationEngine();
        TestController controller = context.getTestController();

        // 计算实际时间秒数 (currentTime是tick计数，每tick = intervalMs毫秒)
        double timeSeconds = currentTime * (intervalMs / 1000.0);

        // 更新仿真温度 — 传递实际秒数
        TemperatureData data = engine.update(timeSeconds);

        // 如果在记录状态，保存数据并增加时间
        if (controller.getCurrentState() == StateMachine.RECORDING) {
            context.addTemperatureData(data);
            // 每800ms增加0.8秒，累计到整数秒时触发tick
            if ((currentTime * intervalMs + intervalMs) % 1000 < intervalMs) {
                controller.tick();
            }
        }

        // 检查Ready状态
        if (controller.getCurrentState() == StateMachine.PREPARING) {
            controller.checkReady();
        }

        // 触发数据更新回调
        if (onDataUpdate != null) {
            onDataUpdate.accept(data);
        }

        currentTime++;
    }

    /**
     * 重置时间计数
     */
    public void resetTime() {
        currentTime = 0;
    }

    // Getter
    public boolean isRunning() { return running; }
    public int getCurrentTime() { return currentTime; }

    // Setter
    public void setOnDataUpdate(Consumer<TemperatureData> callback) { this.onDataUpdate = callback; }
}