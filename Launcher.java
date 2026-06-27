package com.iso11820;

import javafx.application.Application;

/**
 * 启动器类 - 不继承 Application
 * <p>
 * JavaFX 11+ 要求启动类不能直接是 Application 子类（否则在 classpath 模式下
 * 会报 "Error: JavaFX runtime components are missing" 错误）。
 * 因此需要一个单独的 Launcher 类来调用 Application.launch()。
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
