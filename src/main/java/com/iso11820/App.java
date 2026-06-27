package com.iso11820;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主入口
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 初始化全局上下文
        GlobalContext context = GlobalContext.getInstance();
        context.initialize();

        // 加载登录界面
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("ISO 11820 建筑材料不燃性试验仿真系统 - 登录");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        logger.info("应用程序启动");
    }

    @Override
    public void stop() throws Exception {
        // 停止数据采集
        GlobalContext context = GlobalContext.getInstance();
        if (context.getDaqWorker() != null) {
            context.getDaqWorker().stop();
        }
        logger.info("应用程序关闭");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}