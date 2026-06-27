package com.iso11820.ui;

import com.iso11820.GlobalContext;
import com.iso11820.controller.StateMachine;
import com.iso11820.controller.TestController;
import com.iso11820.model.MasterMessage;
import com.iso11820.model.TestMaster;
import com.iso11820.model.TemperatureData;
import com.iso11820.service.DaqWorker;
import com.iso11820.service.SimulationEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面控制器 — 完善按钮状态控制和保护规则
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // 温度显示标签
    @FXML private Label lblTf1;
    @FXML private Label lblTf2;
    @FXML private Label lblTs;
    @FXML private Label lblTc;
    @FXML private Label lblTCal;

    // 温度漂移显示
    @FXML private Label lblDrift;

    // 状态显示
    @FXML private Label lblStatus;
    @FXML private Label lblTimer;
    @FXML private Label lblProductId;
    @FXML private Label lblOperator;

    // 按钮
    @FXML private Button btnNewTest;
    @FXML private Button btnStartHeating;
    @FXML private Button btnStopHeating;
    @FXML private Button btnStartRecord;
    @FXML private Button btnStopRecord;
    @FXML private Button btnRecord;
    @FXML private Button btnSettings;

    // TabPane
    @FXML private TabPane tabPane;
    @FXML private Tab tabTest;
    @FXML private Tab tabQuery;
    @FXML private Tab tabCalibration;

    // 曲线图
    @FXML private LineChart<Number, Number> chartTemp;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    // 消息日志
    @FXML private TextArea txtLog;

    // 曲线数据系列
    private XYChart.Series<Number, Number> seriesTf1;
    private XYChart.Series<Number, Number> seriesTf2;
    private XYChart.Series<Number, Number> seriesTs;
    private XYChart.Series<Number, Number> seriesTc;

    // 全局上下文
    private GlobalContext context;

    // 记录时间
    private int recordTime = 0;

    // 温漂追踪数据（最近10分钟的炉温值，用于线性回归计算漂移）
    private List<Double> tf1DriftWindow = new ArrayList<>();
    private List<Double> tf2DriftWindow = new ArrayList<>();
    private final int DRIFT_WINDOW_SIZE = 750; // 10分钟 = 600秒 / 0.8秒 ≈ 750个tick

    @FXML
    public void initialize() {
        context = GlobalContext.getInstance();

        // 初始化曲线图
        initChart();

        // 显示操作员信息
        lblOperator.setText("操作员: " + context.getCurrentOperator().getUsername());

        // 初始化状态
        updateStatus(StateMachine.IDLE);
        updateButtons(StateMachine.IDLE);

        // 设置回调
        setupCallbacks();

        // 启动数据采集
        context.getDaqWorker().start();

        // 添加初始化消息
        addMessage("系统初始化，操作员：" + context.getCurrentOperator().getUsername());

        logger.info("主界面初始化完成");
    }

    /**
     * 初始化曲线图
     */
    private void initChart() {
        seriesTf1 = new XYChart.Series<>();
        seriesTf1.setName("炉温1");
        seriesTf2 = new XYChart.Series<>();
        seriesTf2.setName("炉温2");
        seriesTs = new XYChart.Series<>();
        seriesTs.setName("表面温");
        seriesTc = new XYChart.Series<>();
        seriesTc.setName("中心温");

        chartTemp.getData().addAll(seriesTf1, seriesTf2, seriesTs, seriesTc);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(800);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(600); // 显示最近10分钟(600秒)
    }

    /**
     * 设置回调
     */
    private void setupCallbacks() {
        TestController testController = context.getTestController();
        DaqWorker daqWorker = context.getDaqWorker();

        // 状态变化回调
        testController.setOnStateChanged(state -> {
            Platform.runLater(() -> {
                updateStatus(state);
                updateButtons(state);
            });
        });

        // 消息回调
        testController.setOnMessage(msg -> {
            Platform.runLater(() -> addMessage(msg.getMessage()));
        });

        // 数据更新回调
        daqWorker.setOnDataUpdate(data -> {
            Platform.runLater(() -> updateTemperatureDisplay(data));
        });

        // 记录完成回调
        testController.setOnRecordComplete(() -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("试验完成");
                alert.setHeaderText("试验记录已完成");
                alert.setContentText("请点击「试验记录」保存试验结果");
                alert.showAndWait();
            });
        });
    }

    /**
     * 更新温度显示
     */
    private void updateTemperatureDisplay(TemperatureData data) {
        lblTf1.setText(String.format("%.1f °C", data.getTf1()));
        lblTf2.setText(String.format("%.1f °C", data.getTf2()));
        lblTs.setText(String.format("%.1f °C", data.getTs()));
        lblTc.setText(String.format("%.1f °C", data.getTc()));
        lblTCal.setText(String.format("%.1f °C", data.getTCal()));

        // 更新温漂追踪数据
        tf1DriftWindow.add(data.getTf1());
        tf2DriftWindow.add(data.getTf2());
        if (tf1DriftWindow.size() > DRIFT_WINDOW_SIZE) {
            tf1DriftWindow.remove(0);
        }
        if (tf2DriftWindow.size() > DRIFT_WINDOW_SIZE) {
            tf2DriftWindow.remove(0);
        }

        // 计算并显示温度漂移（°C/10min）
        if (tf1DriftWindow.size() >= 100) { // 至少需要一定数据量才能计算
            double drift = calculateDrift(tf1DriftWindow);
            lblDrift.setText(String.format("%.2f °C/10min", drift));
        } else {
            lblDrift.setText("-- °C/10min");
        }

        // 更新曲线
        if (context.getTestController().getCurrentState() == StateMachine.RECORDING) {
            recordTime = context.getTestController().getElapsedTime();
            lblTimer.setText(String.valueOf(recordTime));

            seriesTf1.getData().add(new XYChart.Data<>(recordTime, data.getTf1()));
            seriesTf2.getData().add(new XYChart.Data<>(recordTime, data.getTf2()));
            seriesTs.getData().add(new XYChart.Data<>(recordTime, data.getTs()));
            seriesTc.getData().add(new XYChart.Data<>(recordTime, data.getTc()));

            // 滚动X轴显示最近10分钟
            if (recordTime > 600) {
                xAxis.setLowerBound(recordTime - 600);
                xAxis.setUpperBound(recordTime);
            }
        }
    }

    /**
     * 计算温度漂移 — 对最近数据做简单线性回归，返回斜率（°C/10分钟）
     * 使用最小二乘法：slope = Σ(xi*yi) - n*x̄*ȳ / Σ(xi²) - n*x̄²
     * 将slope从每tick的斜率转换为每10分钟的斜率
     */
    private double calculateDrift(List<Double> tempWindow) {
        int n = tempWindow.size();
        if (n < 2) return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i; // tick index
            double y = tempWindow.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double xMean = sumX / n;
        double yMean = sumY / n;

        double denominator = sumX2 - n * xMean * xMean;
        if (denominator == 0) return 0;

        double slopePerTick = (sumXY - n * xMean * yMean) / denominator;

        // 每tick = 800ms = 0.8秒，10分钟 = 600秒 = 750个tick
        double slopePerTenMin = slopePerTick * 750;

        return slopePerTenMin;
    }

    /**
     * 更新状态显示
     */
    private void updateStatus(StateMachine state) {
        lblStatus.setText(state.getDisplayName());
        lblStatus.setStyle(getStatusStyle(state));
    }

    private String getStatusStyle(StateMachine state) {
        switch (state) {
            case IDLE: return "-fx-text-fill: gray;";
            case PREPARING: return "-fx-text-fill: orange;";
            case READY: return "-fx-text-fill: green;";
            case RECORDING: return "-fx-text-fill: red;";
            case COMPLETE: return "-fx-text-fill: blue;";
            default: return "";
        }
    }

    /**
     * 更新按钮状态 — 严格按照状态机 + 保护规则
     *
     * 按钮启用规则（对照开发文档2.7节）：
     * | 按钮         | Idle | Preparing | Ready | Recording | Complete |
     * |-------------|:----:|:---------:|:-----:|:---------:|:--------:|
     * | 新建试验     | ✅   | 有活动❌   | ❌    | ❌        | 未保存❌  |
     * | 开始升温     | ✅   | ❌        | ❌    | ❌        | ❌       |
     * | 停止升温     | ❌   | ✅        | ✅    | ❌        | ✅       |
     * | 开始记录     | ❌   | ❌        | ✅    | ❌        | ❌       |
     * | 停止记录     | ❌   | ❌        | ❌    | ✅        | ❌       |
     * | 试验记录     | ❌   | ❌        | ❌    | ❌        | ✅(未保存)|
     * | 参数设置     | ✅   | ✅        | ✅    | ❌        | ✅       |
     *
     * 保护规则：有 totaltesttime>0 且 flag!="10000000" 时，
     *           禁止新建试验和开始记录，只允许保存试验记录
     */
    private void updateButtons(StateMachine state) {
        // 基本状态规则
        switch (state) {
            case IDLE:
                btnNewTest.setDisable(false);
                btnStartHeating.setDisable(false);
                btnStopHeating.setDisable(true);
                btnStartRecord.setDisable(true);
                btnStopRecord.setDisable(true);
                btnRecord.setDisable(true);
                btnSettings.setDisable(false);
                break;

            case PREPARING:
                btnNewTest.setDisable(true);
                btnStartHeating.setDisable(true);
                btnStopHeating.setDisable(false);
                btnStartRecord.setDisable(true);
                btnStopRecord.setDisable(true);
                btnRecord.setDisable(true);
                btnSettings.setDisable(false);
                break;

            case READY:
                btnNewTest.setDisable(true);
                btnStartHeating.setDisable(true);
                btnStopHeating.setDisable(false);
                btnStartRecord.setDisable(false);
                btnStopRecord.setDisable(true);
                btnRecord.setDisable(true);
                btnSettings.setDisable(false);
                break;

            case RECORDING:
                btnNewTest.setDisable(true);
                btnStartHeating.setDisable(true);
                btnStopHeating.setDisable(true);
                btnStartRecord.setDisable(true);
                btnStopRecord.setDisable(false);
                btnRecord.setDisable(true);
                btnSettings.setDisable(true);
                break;

            case COMPLETE:
                btnNewTest.setDisable(true); // 默认禁用，保存后变为可用
                btnStartHeating.setDisable(true);
                btnStopHeating.setDisable(false); // COMPLETE可以停止升温
                btnStartRecord.setDisable(true);
                btnStopRecord.setDisable(true);
                btnRecord.setDisable(false); // COMPLETE可以保存试验记录
                btnSettings.setDisable(false);
                break;
        }

        // 保护规则：如果有未保存的试验（totaltesttime>0 且 flag不是10000000）
        // 禁止新建试验和开始记录，只允许保存试验记录
        TestMaster test = context.getCurrentTest();
        if (test != null && test.getTotaltesttime() > 0 && !test.isSaved()) {
            btnNewTest.setDisable(true);
            btnStartHeating.setDisable(true);
            btnStartRecord.setDisable(true);
            btnRecord.setDisable(false); // 必须允许保存
        }

        // 保存完成后允许新建试验
        if (test != null && test.isSaved()) {
            btnNewTest.setDisable(false);
            btnRecord.setDisable(true); // 已保存，不再需要点试验记录
        }
    }

    /**
     * 添加消息
     */
    private void addMessage(String msg) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        txtLog.appendText(time + "  " + msg + "\n");
    }

    // ========== 按钮事件 ==========

    @FXML
    public void onNewTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new_test.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("新建试验");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            // 更新样品编号显示
            TestMaster test = context.getCurrentTest();
            if (test != null) {
                lblProductId.setText("样品编号: " + test.getProductid());
            }

        } catch (Exception e) {
            logger.error("打开新建试验窗口失败", e);
        }
    }

    @FXML
    public void onStartHeating() {
        // 保护规则：必须有当前试验才能开始升温
        if (context.getCurrentTest() == null) {
            showAlert("请先新建试验再开始升温");
            return;
        }

        context.getTestController().startHeating();
        context.getSimulationEngine().startHeating();
    }

    @FXML
    public void onStopHeating() {
        // COMPLETE状态下停止升温：炉子冷却，回到IDLE
        if (context.getTestController().getCurrentState() == StateMachine.COMPLETE) {
            context.getTestController().stopHeatingFromComplete();
            context.getSimulationEngine().stopHeating();
            context.getDaqWorker().resetTime();
            return;
        }

        context.getTestController().stopHeating();
        context.getSimulationEngine().stopHeating();
        context.getDaqWorker().resetTime();
    }

    @FXML
    public void onStartRecord() {
        context.getTestController().startRecording();
        context.getDaqWorker().resetTime();

        // 清空曲线数据
        seriesTf1.getData().clear();
        seriesTf2.getData().clear();
        seriesTs.getData().clear();
        seriesTc.getData().clear();

        // 清空温漂追踪数据
        tf1DriftWindow.clear();
        tf2DriftWindow.clear();
    }

    @FXML
    public void onStopRecord() {
        context.getTestController().stopRecording();
    }

    @FXML
    public void onRecord() {
        // 保护规则：必须有当前试验才能打开试验记录窗口
        if (context.getCurrentTest() == null) {
            showAlert("没有当前试验");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/record.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("试验记录");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 440, 550);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            // 保存后更新按钮状态
            TestMaster test = context.getCurrentTest();
            if (test != null && test.isSaved()) {
                btnRecord.setDisable(true);
                btnNewTest.setDisable(false);
                lblProductId.setText("样品编号: -");
                context.setCurrentTest(null);
                // 保存后回到IDLE，允许开始新试验
                context.getTestController().resetToIdle();
            }

        } catch (Exception e) {
            logger.error("打开试验记录窗口失败", e);
        }
    }

    @FXML
    public void onSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("参数设置");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("打开参数设置窗口失败", e);
        }
    }

    @FXML
    public void onExportExcel() {
        TestMaster test = context.getCurrentTest();
        if (test == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setContentText("没有试验数据可导出");
            alert.showAndWait();
            return;
        }

        String path = context.getExportService().exportExcel(
            test.getTestid(),
            test,
            context.getTemperatureDataList()
        );

        if (path != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("导出成功");
            alert.setContentText("Excel文件已保存到: " + path);
            alert.showAndWait();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
