package com.iso11820.ui;

import com.iso11820.GlobalContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 设备校准界面控制器
 */
public class CalibrationController {
    private static final Logger logger = LoggerFactory.getLogger(CalibrationController.class);

    @FXML private Label lblCalTemp;
    @FXML private ComboBox<String> cmbCalType;
    @FXML private TextField txtTempPoint;
    @FXML private TextArea txtRemarks;
    @FXML private Button btnRecord;
    @FXML private Button btnSave;

    @FXML private TableView<CalRecord> tableHistory;
    @FXML private TableColumn<CalRecord, String> colDate;
    @FXML private TableColumn<CalRecord, String> colType;
    @FXML private TableColumn<CalRecord, String> colOperator;

    private final GlobalContext context = GlobalContext.getInstance();

    @FXML
    public void initialize() {
        cmbCalType.getItems().addAll("Surface", "Center");
        cmbCalType.setValue("Surface");

        loadHistory();
        logger.info("设备校准界面初始化");
    }

    private void loadHistory() {
        // 加载历史校准记录
        var history = context.getDbHelper().queryCalibrationHistory();
        tableHistory.getItems().clear();
        for (Object[] row : history) {
            tableHistory.getItems().add(new CalRecord(
                (String) row[1],
                (String) row[2],
                (String) row[3]
            ));
        }
    }

    @FXML
    public void onRecord() {
        // 记录当前校准温度点
        double calTemp = context.getSimulationEngine().getTCal();
        lblCalTemp.setText(String.format("%.1f °C", calTemp));

        if (!txtTempPoint.getText().isEmpty()) {
            txtTempPoint.appendText(", ");
        }
        txtTempPoint.appendText(String.format("%.1f", calTemp));
    }

    @FXML
    public void onSave() {
        String type = cmbCalType.getValue();
        String tempData = txtTempPoint.getText();
        String remarks = txtRemarks.getText();

        if (tempData.isEmpty()) {
            showAlert("请先记录温度点");
            return;
        }

        String id = UUID.randomUUID().toString();
        String operator = context.getCurrentOperator().getUsername();
        int apparatusId = context.getCurrentApparatus().getApparatusid();

        boolean success = context.getDbHelper().insertCalibration(
            id,
            java.time.LocalDateTime.now().toString(),
            type,
            apparatusId,
            operator,
            tempData,
            remarks
        );

        if (success) {
            showAlert("校准记录保存成功");
            loadHistory();
            txtTempPoint.clear();
            txtRemarks.clear();
        } else {
            showAlert("保存失败");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 表格数据模型
    public static class CalRecord {
        private String date;
        private String type;
        private String operator;

        public CalRecord(String date, String type, String operator) {
            this.date = date;
            this.type = type;
            this.operator = operator;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public String getOperator() { return operator; }
    }
}