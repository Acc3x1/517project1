package com.iso11820.model;

/**
 * 仿真生成的试验现象数据 — 模拟真实实验中操作员观察到的结果
 *
 * 由于是仿真系统（没有真实样品在烧），试验后质量和火焰现象需要由程序模拟生成，
 * 模拟真实操作员在实验结束后称重和观察火焰得到的数据。
 */
public class SimulatedPhenomenon {
    private final double postWeight;        // 试验后质量(g)
    private final boolean hasFlame;         // 是否出现持续火焰
    private final int flameTime;            // 火焰发生时刻(秒)
    private final int flameDuration;        // 火焰持续时间(秒)
    private final boolean passing;          // 本次模拟是否判定为合格

    public SimulatedPhenomenon(double postWeight, boolean hasFlame,
                               int flameTime, int flameDuration, boolean passing) {
        this.postWeight = postWeight;
        this.hasFlame = hasFlame;
        this.flameTime = flameTime;
        this.flameDuration = flameDuration;
        this.passing = passing;
    }

    public double getPostWeight() { return postWeight; }
    public boolean isHasFlame() { return hasFlame; }
    public int getFlameTime() { return flameTime; }
    public int getFlameDuration() { return flameDuration; }
    public boolean isPassing() { return passing; }
}
