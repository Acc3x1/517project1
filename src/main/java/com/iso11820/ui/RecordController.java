package com.iso11820.ui;

import com.iso11820.GlobalContext;
import com.iso11820.model.SimulatedPhenomenon;
import com.iso11820.model.TestMaster;
import com.iso11820.model.TemperatureData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 试验记录保存界面控制器
 */
public class RecordController {
    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @FXML private CheckBox chkFlame;
    @FXML private TextField txtFlameTime;
    @FXML private TextField txtFlameDuration;
    @FXML private TextField txtPostWeight;
    @FXML private TextArea txtMemo;

    @FXML private Label lblPreWeight;
    @FXML private Label lblLostWeight;
    @FXML private Label lblLostWeightPer;
    @FXML private Label lblDeltaT;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnAutoGenerate;

    private final GlobalContext context = GlobalContext.getInstance();

    @FXML
    public void initialize() {
        TestMaster test = context.getCurrentTest();
        if (test != null) {
            lblPreWeight.setText(String.valueOf(test.getPreweight()) + " g");
        }

        // 火焰选项联动
        chkFlame.selectedProperty().addListener((obs, old, newVal) -> {
            txtFlameTime.setDisable(!newVal);
            txtFlameDuration.setDisable(!newVal);
        });

        txtFlameTime.setDisable(true);
        txtFlameDuration.setDisable(true);

        // 试验后质量输入联动计算
        txtPostWeight.textProperty().addListener((obs, old, newVal) -> {
            calculateResults();
        });

        logger.info("试验记录界面初始化");
    }

    /**
     * 自动生成现象数据 — 调用仿真引擎生成合理的试验结果
     */
    @FXML
    public void onAutoGenerate() {
        TestMaster test = context.getCurrentTest();
        if (test == null) {
            showAlert("没有当前试验");
            return;
        }

        // 调用仿真引擎生成现象数据
        SimulatedPhenomenon phenomenon = context.getSimulationEngine().simulatePhenomenon(
            test.getPreweight(),
            context.getTestController().getElapsedTime()
        );

        // 填充表单字段
        txtPostWeight.setText(String.format("%.2f", phenomenon.getPostWeight()));
        chkFlame.setSelected(phenomenon.isHasFlame());

        if (phenomenon.isHasFlame()) {
            txtFlameTime.setText(String.valueOf(phenomenon.getFlameTime()));
            txtFlameDuration.setText(String.valueOf(phenomenon.getFlameDuration()));
            txtFlameTime.setDisable(false);
            txtFlameDuration.setDisable(false);
        } else {
            txtFlameTime.clear();
            txtFlameDuration.clear();
            txtFlameTime.setDisable(true);
            txtFlameDuration.setDisable(true);
        }

        // 触发计算，更新失重量、失重率、温升显示
        calculateResults();

        // 提示用户
        String resultText = phenomenon.isPassing() ? "合格（不燃）" : "不合格（可燃）";
        String message = String.format("自动生成完成\n\n本次模拟结果：%s\n- 失重率: %.1f%%\n- 火焰: %s\n\n⚠️ 重要：请检查数据后点击「保存记录」按钮，\n否则数据不会保存到数据库！",
            resultText,
            phenomenon.isHasFlame() ? ((test.getPreweight() - phenomenon.getPostWeight()) / test.getPreweight() * 100) : 0,
            phenomenon.isHasFlame() ? String.format("%d秒时出现，持续%d秒", phenomenon.getFlameTime(), phenomenon.getFlameDuration()) : "无"
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("自动生成完成");
        alert.setHeaderText("现象数据已自动生成");
        alert.setContentText(message);
        alert.showAndWait();

        logger.info("自动生成现象数据: postWeight={}, hasFlame={}, flameTime={}, flameDuration={}, passing={}",
            phenomenon.getPostWeight(), phenomenon.isHasFlame(), phenomenon.getFlameTime(),
            phenomenon.getFlameDuration(), phenomenon.isPassing());
    }

    private void calculateResults() {
        TestMaster test = context.getCurrentTest();
        if (test == null) return;

        try {
            double postWeight = Double.parseDouble(txtPostWeight.getText().trim());
            double preWeight = test.getPreweight();

            double lostWeight = preWeight - postWeight;
            double lostWeightPer = (lostWeight / preWeight) * 100;

            lblLostWeight.setText(String.format("%.2f g", lostWeight));
            lblLostWeightPer.setText(String.format("%.2f%%", lostWeightPer));

            // 计算温升
            List<TemperatureData> data = context.getTemperatureDataList();
            if (!data.isEmpty()) {
                TemperatureData first = data.get(0);
                TemperatureData last = data.get(data.size() - 1);

                double deltaTf1 = last.getTf1() - first.getTf1();
                double deltaTf2 = last.getTf2() - first.getTf2();
                double deltaTs = last.getTs() - first.getTs();
                double deltaTc = last.getTc() - first.getTc();

                lblDeltaT.setText(String.format("%.2f °C", deltaTs));
            }
        } catch (NumberFormatException e) {
            lblLostWeight.setText("-");
            lblLostWeightPer.setText("-");
            lblDeltaT.setText("-");
        }
    }

    @FXML
    public void onSave() {
        TestMaster test = context.getCurrentTest();
        if (test == null) {
            showAlert("没有当前试验");
            return;
        }

        // 验证试验后质量
        double postWeight;
        try {
            postWeight = Double.parseDouble(txtPostWeight.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("请输入有效的试验后质量");
            return;
        }

        // 计算结果
        double preWeight = test.getPreweight();
        double lostWeight = preWeight - postWeight;
        double lostWeightPer = (lostWeight / preWeight) * 100;

        // 火焰信息
        int flameTime = 0;
        int flameDuration = 0;
        if (chkFlame.isSelected()) {
            try {
                flameTime = Integer.parseInt(txtFlameTime.getText().trim());
                flameDuration = Integer.parseInt(txtFlameDuration.getText().trim());
            } catch (NumberFormatException e) {
                showAlert("请输入有效的火焰时间");
                return;
            }
        }

        // 获取温度统计数据 — time字段从double转为int存入数据库
        List<TemperatureData> data = context.getTemperatureDataList();
        double maxTf1 = 0, maxTf2 = 0, maxTs = 0, maxTc = 0;
        int maxTf1Time = 0, maxTf2Time = 0, maxTsTime = 0, maxTcTime = 0;
        double finalTf1 = 0, finalTf2 = 0, finalTs = 0, finalTc = 0;
        int finalTf1Time = 0, finalTf2Time = 0, finalTsTime = 0, finalTcTime = 0;
        double deltaTf1 = 0, deltaTf2 = 0, deltaDs = 0, deltaDc = 0;

        if (!data.isEmpty()) {
            TemperatureData first = data.get(0);
            TemperatureData last = data.get(data.size() - 1);

            finalTf1 = last.getTf1();
            finalTf2 = last.getTf2();
            finalTs = last.getTs();
            finalTc = last.getTc();
            finalTf1Time = (int) last.getTime();
            finalTf2Time = (int) last.getTime();
            finalTsTime = (int) last.getTime();
            finalTcTime = (int) last.getTime();

            deltaTf1 = finalTf1 - test.getAmbtemp();
            deltaTf2 = finalTf2 - test.getAmbtemp();
            deltaDs = finalTs - test.getAmbtemp();
            deltaDc = finalTc - test.getAmbtemp();

            // 计算最大值
            for (TemperatureData td : data) {
                if (td.getTf1() > maxTf1) { maxTf1 = td.getTf1(); maxTf1Time = (int) td.getTime(); }
                if (td.getTf2() > maxTf2) { maxTf2 = td.getTf2(); maxTf2Time = (int) td.getTime(); }
                if (td.getTs() > maxTs) { maxTs = td.getTs(); maxTsTime = (int) td.getTime(); }
                if (td.getTc() > maxTc) { maxTc = td.getTc(); maxTcTime = (int) td.getTime(); }
            }
        }

        String phenocode = chkFlame.isSelected() ? "FLAME" : "";
        String memo = txtMemo.getText();

        int totalTime = context.getTestController().getElapsedTime();

        // 保存到数据库 — deltaDs/deltaDc即deltaTs/deltaTc
        boolean success = context.getDbHelper().updateTestResult(
            test.getProductid(),
            test.getTestid(),
            postWeight,
            lostWeight,
            lostWeightPer,
            deltaDs,
            totalTime,
            phenocode,
            flameTime,
            flameDuration,
            maxTf1, maxTf2, maxTs, maxTc,
            maxTf1Time, maxTf2Time, maxTsTime, maxTcTime,
            finalTf1, finalTf2, finalTs, finalTc,
            finalTf1Time, finalTf2Time, finalTsTime, finalTcTime,
            deltaTf1, deltaTf2, deltaDs, deltaDc,
            memo
        );

        if (success) {
            // 导出CSV
            String csvPath = context.getExportService().exportCsv(
                test.getProductid(),
                test.getTestid(),
                data
            );

            // 导出PDF
            test.setPostweight(postWeight);
            test.setLostweight(lostWeight);
            test.setLostweightPer(lostWeightPer);
            test.setDeltats(deltaDs);
            test.setDeltatc(deltaDc);
            test.setDeltatf1(deltaTf1);
            test.setDeltatf2(deltaTf2);
            test.setTotaltesttime(totalTime);
            test.setFlametime(flameTime);
            test.setFlameduration(flameDuration);

            String pdfPath = context.getExportService().exportPdf(
                test.getTestid(),
                test,
                data
            );

            // 标记已保存
            test.setFlag("10000000");

            logger.info("试验记录保存成功: {}", test.getTestid());

            showAlert("试验记录保存成功\nCSV: " + csvPath + "\nPDF: " + pdfPath);
            closeWindow();
        } else {
            showAlert("保存失败，请检查数据库");
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