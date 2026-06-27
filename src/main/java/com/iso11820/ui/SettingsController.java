package com.iso11820.ui;

import com.iso11820.GlobalContext;
import com.iso11820.config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 参数设置界面控制器 — 修改后立即同步到仿真引擎和试验控制器
 */
public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML private TextField txtTargetTemp;
    @FXML private TextField txtHeatingRate;
    @FXML private TextField txtFluctuation;
    @FXML private TextField txtStableThreshold;
    @FXML private TextField txtDuration;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private final GlobalContext context = GlobalContext.getInstance();

    @FXML
    public void initialize() {
        AppConfig.SimulationConfig sim = context.getConfig().getSimulation();
        AppConfig.CriteriaConfig criteria = context.getConfig().getCriteria();

        txtTargetTemp.setText(String.valueOf(sim.getTargetFurnaceTemp()));
        txtHeatingRate.setText(String.valueOf(sim.getHeatingRatePerSecond()));
        txtFluctuation.setText(String.valueOf(sim.getTempFluctuation()));
        txtStableThreshold.setText(String.valueOf(sim.getStableThreshold()));
        txtDuration.setText(String.valueOf(criteria.getTargetDurationSeconds()));

        logger.info("参数设置界面初始化");
    }

    @FXML
    public void onSave() {
        try {
            double targetTemp = Double.parseDouble(txtTargetTemp.getText().trim());
            double heatingRate = Double.parseDouble(txtHeatingRate.getText().trim());
            double fluctuation = Double.parseDouble(txtFluctuation.getText().trim());
            double stableThreshold = Double.parseDouble(txtStableThreshold.getText().trim());
            int duration = Integer.parseInt(txtDuration.getText().trim());

            // 立即同步到仿真引擎 — 所有参数即时生效
            context.getSimulationEngine().setTargetTemp(targetTemp);
            context.getSimulationEngine().setHeatingRate(heatingRate);
            context.getSimulationEngine().setFluctuation(fluctuation);
            context.getSimulationEngine().setStableThreshold(stableThreshold);

            // 同步到试验控制器
            context.getTestController().setTargetDuration(duration);

            // 同步到配置对象（后续导出报告等会使用）
            AppConfig.SimulationConfig sim = context.getConfig().getSimulation();
            sim.setTargetFurnaceTemp(targetTemp);
            sim.setHeatingRatePerSecond(heatingRate);
            sim.setTempFluctuation(fluctuation);
            sim.setStableThreshold(stableThreshold);

            AppConfig.CriteriaConfig criteria = context.getConfig().getCriteria();
            criteria.setTargetDurationSeconds(duration);

            logger.info("参数设置已更新并同步: 目标温度={}, 升温速率={}, 波动={}, 稳定阈值={}, 时长={}",
                targetTemp, heatingRate, fluctuation, stableThreshold, duration);

            showAlert("参数已更新并即时生效\n目标温度: " + targetTemp + "°C\n升温速率: " + heatingRate + "°C/s\n波动: " + fluctuation + "°C\n稳定阈值: " + stableThreshold + "°C\n试验时长: " + duration + "秒");
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("请输入有效的数值");
        }
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
