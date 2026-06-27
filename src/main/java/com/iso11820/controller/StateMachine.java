package com.iso11820.controller;

/**
 * 试验状态机 - 5状态定义
 */
public enum StateMachine {
    IDLE("空闲", "系统空闲，等待开始"),
    PREPARING("升温中", "系统升温中"),
    READY("就绪", "温度已稳定，可以开始记录"),
    RECORDING("记录中", "正在记录温度数据"),
    COMPLETE("完成", "试验完成，等待保存");

    private final String displayName;
    private final String description;

    StateMachine(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否可以从当前状态转换到目标状态
     */
    public boolean canTransitionTo(StateMachine target) {
        switch (this) {
            case IDLE:
                return target == PREPARING;
            case PREPARING:
                return target == READY || target == IDLE;
            case READY:
                return target == RECORDING || target == PREPARING || target == IDLE;
            case RECORDING:
                return target == COMPLETE || target == PREPARING;
            case COMPLETE:
                return target == PREPARING || target == IDLE;
            default:
                return false;
        }
    }
}