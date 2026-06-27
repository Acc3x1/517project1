package com.iso11820.controller;

import com.iso11820.GlobalContext;
import com.iso11820.config.AppConfig;
import com.iso11820.model.TestMaster;
import com.iso11820.model.MasterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * 试验控制器 - 状态流转控制
 */
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final GlobalContext context;
    private StateMachine currentState = StateMachine.IDLE;

    // 计时器
    private int elapsedTime = 0; // 已记录秒数

    // 目标时长
    private int targetDuration = 3600; // 默认60分钟

    // 状态变化回调
    private Consumer<StateMachine> onStateChanged;

    // 消息回调
    private Consumer<MasterMessage> onMessage;

    // 记录完成回调
    private Runnable onRecordComplete;

    public TestController(GlobalContext context) {
        this.context = context;
        AppConfig.CriteriaConfig criteria = context.getConfig().getCriteria();
        if (criteria != null) {
            this.targetDuration = criteria.getTargetDurationSeconds();
        }
    }

    /**
     * 开始升温
     */
    public boolean startHeating() {
        if (!currentState.canTransitionTo(StateMachine.PREPARING)) {
            logger.warn("当前状态 {} 无法开始升温", currentState);
            return false;
        }

        setState(StateMachine.PREPARING);
        context.getSimulationEngine().startHeating();
        sendMessage("开始升温，系统升温中");
        return true;
    }

    /**
     * 停止升温 — 支持从 PREPARING、READY 和 COMPLETE 状态停止
     */
    public boolean stopHeating() {
        if (currentState == StateMachine.PREPARING || currentState == StateMachine.READY) {
            setState(StateMachine.IDLE);
            context.getSimulationEngine().stopHeating();
            sendMessage("停止升温，系统回到空闲状态");
            return true;
        }
        return false;
    }

    /**
     * 从 COMPLETE 状态停止升温 — 炉子冷却回到 IDLE
     */
    public boolean stopHeatingFromComplete() {
        if (currentState == StateMachine.COMPLETE) {
            setState(StateMachine.IDLE);
            context.getSimulationEngine().stopHeating();
            sendMessage("停止升温，炉子冷却，系统回到空闲状态");
            return true;
        }
        return false;
    }

    /**
     * 保存试验记录后重置到 IDLE — 允许开始下一次试验
     */
    public void resetToIdle() {
        setState(StateMachine.IDLE);
        elapsedTime = 0;
        sendMessage("试验已保存，系统回到空闲状态");
    }

    /**
     * 检查是否可以开始记录
     */
    public void checkReady() {
        if (currentState == StateMachine.PREPARING) {
            if (context.getSimulationEngine().canStartRecording()) {
                setState(StateMachine.READY);
                sendMessage("温度已稳定，可以开始记录");
            }
        }
    }

    /**
     * 开始记录
     */
    public boolean startRecording() {
        if (!currentState.canTransitionTo(StateMachine.RECORDING)) {
            logger.warn("当前状态 {} 无法开始记录", currentState);
            return false;
        }

        setState(StateMachine.RECORDING);
        context.getSimulationEngine().startRecording();
        context.clearTemperatureData();
        elapsedTime = 0;
        sendMessage("开始记录，计时开始");
        return true;
    }

    /**
     * 停止记录
     */
    public boolean stopRecording() {
        if (currentState != StateMachine.RECORDING) {
            return false;
        }

        setState(StateMachine.COMPLETE);
        context.getSimulationEngine().stopRecording();
        sendMessage("用户手动停止记录");

        if (onRecordComplete != null) {
            onRecordComplete.run();
        }
        return true;
    }

    /**
     * 每秒更新 - 由 DaqWorker 调用
     */
    public void tick() {
        if (currentState == StateMachine.RECORDING) {
            elapsedTime++;

            // 检查是否达到目标时长
            if (elapsedTime >= targetDuration) {
                setState(StateMachine.COMPLETE);
                context.getSimulationEngine().stopRecording();
                sendMessage("记录时间到达 " + targetDuration + " 秒，试验自动结束");

                if (onRecordComplete != null) {
                    onRecordComplete.run();
                }
            }

            // 标准模式下，每5分钟检查终止条件
            if (context.getConfig().getCriteria().isStandardMode()) {
                int checkPoints[] = {1800, 2100, 2400, 2700, 3000, 3300}; // 30,35,40,45,50,55分钟
                for (int cp : checkPoints) {
                    if (elapsedTime == cp) {
                        // 检查温漂条件（简化版）
                        if (checkEarlyTermination()) {
                            setState(StateMachine.COMPLETE);
                            context.getSimulationEngine().stopRecording();
                            sendMessage("满足终止条件，试验结束");

                            if (onRecordComplete != null) {
                                onRecordComplete.run();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查是否满足提前终止条件
     */
    private boolean checkEarlyTermination() {
        // 简化版：检查温度稳定性
        double maxDrift = context.getConfig().getCriteria().getMaxTemperatureDriftPerTenMinutes();
        // 这里简化处理，假设稳定时可以终止
        return context.getSimulationEngine().isStable();
    }

    /**
     * 试验完成后继续升温（为下一次试验准备）
     */
    public void continueHeating() {
        if (currentState == StateMachine.COMPLETE) {
            setState(StateMachine.PREPARING);
            context.getSimulationEngine().startHeating();
            sendMessage("试验完成，保持炉温等待下次试验");
        }
    }

    /**
     * 设置状态
     */
    private void setState(StateMachine newState) {
        logger.info("状态变化: {} -> {}", currentState, newState);
        currentState = newState;
        if (onStateChanged != null) {
            onStateChanged.accept(newState);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(String msg) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        MasterMessage message = new MasterMessage(time, msg);
        context.addMessage(time, msg);
        if (onMessage != null) {
            onMessage.accept(message);
        }
    }

    // Getters
    public StateMachine getCurrentState() { return currentState; }
    public int getElapsedTime() { return elapsedTime; }
    public int getTargetDuration() { return targetDuration; }

    // Setters
    public void setTargetDuration(int duration) { this.targetDuration = duration; }

    // 回调设置
    public void setOnStateChanged(Consumer<StateMachine> callback) { this.onStateChanged = callback; }
    public void setOnMessage(Consumer<MasterMessage> callback) { this.onMessage = callback; }
    public void setOnRecordComplete(Runnable callback) { this.onRecordComplete = callback; }
}