package com.iso11820.ui;

import com.iso11820.GlobalContext;
import com.iso11820.model.TestMaster;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 新建试验界面控制器
 */
public class NewTestController {
    private static final Logger logger = LoggerFactory.getLogger(NewTestController.class);

    // 样品信息
    @FXML private TextField txtProductId;
    @FXML private TextField txtProductName;
    @FXML private TextField txtSpecific;
    @FXML private TextField txtDiameter;
    @FXML private TextField txtHeight;

    // 试验参数
    @FXML private TextField txtAmbTemp;
    @FXML private TextField txtAmbHumi;
    @FXML private TextField txtPreWeight;
    @FXML private ComboBox<String> cmbDuration;
    @FXML private Label lblOperator;

    // 设备信息
    @FXML private Label lblApparatusId;
    @FXML private Label lblApparatusName;
    @FXML private Label lblCheckDate;

    @FXML private Button btnCreate;
    @FXML private Button btnCancel;

    private final GlobalContext context = GlobalContext.getInstance();

    @FXML
    public void initialize() {
        // 显示操作员
        lblOperator.setText(context.getCurrentOperator().getUsername());

        // 显示设备信息
        var apparatus = context.getCurrentApparatus();
        if (apparatus != null) {
            lblApparatusId.setText(apparatus.getInnernumber());
            lblApparatusName.setText(apparatus.getApparatusname());
            lblCheckDate.setText(apparatus.getCheckdatef());
        }

        // 设置时长选项
        cmbDuration.getItems().addAll("标准60分钟", "30分钟", "90分钟", "自定义");
        cmbDuration.setValue("标准60分钟");

        // 默认值
        txtAmbTemp.setText("25.0");
        txtAmbHumi.setText("50.0");
        txtPreWeight.setText("100.0");

        logger.info("新建试验界面初始化");
    }

    @FXML
    public void onCreate() {
        // 验证输入
        String productId = txtProductId.getText().trim();
        if (productId.isEmpty()) {
            showAlert("请输入样品编号");
            return;
        }

        String productName = txtProductName.getText().trim();
        if (productName.isEmpty()) {
            showAlert("请输入样品名称");
            return;
        }

        double preWeight;
        try {
            preWeight = Double.parseDouble(txtPreWeight.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("试验前质量必须是数字");
            return;
        }

        double ambTemp;
        try {
            ambTemp = Double.parseDouble(txtAmbTemp.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("环境温度必须是数字");
            return;
        }

        double ambHumi;
        try {
            ambHumi = Double.parseDouble(txtAmbHumi.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("环境湿度必须是数字");
            return;
        }

        // 生成试验ID
        String testId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // 创建试验记录
        TestMaster test = new TestMaster();
        test.setProductid(productId);
        test.setTestid(testId);
        test.setOperator(context.getCurrentOperator().getUsername());
        test.setAmbtemp(ambTemp);
        test.setAmbhumi(ambHumi);
        test.setPreweight(preWeight);
        test.setApparatusid(context.getCurrentApparatus().getInnernumber());
        test.setApparatusname(context.getCurrentApparatus().getApparatusname());
        test.setApparatuschkdate(context.getCurrentApparatus().getCheckdatef());

        // 保存到数据库
        boolean success = context.getDbHelper().insertTest(
            productId,
            testId,
            test.getOperator(),
            preWeight,
            ambTemp,
            ambHumi,
            test.getApparatusid(),
            test.getApparatusname(),
            test.getApparatuschkdate()
        );

        if (success) {
            // 插入样品信息
            try {
                double diameter = Double.parseDouble(txtDiameter.getText().trim());
                double height = Double.parseDouble(txtHeight.getText().trim());
                context.getDbHelper().insertProduct(productId, productName, txtSpecific.getText().trim(), diameter, height);
            } catch (NumberFormatException e) {
                logger.warn("样品尺寸解析失败，跳过");
            }

            context.setCurrentTest(test);

            // 设置试验时长
            String duration = cmbDuration.getValue();
            if (duration.equals("30分钟")) {
                context.getTestController().setTargetDuration(1800);
            } else if (duration.equals("90分钟")) {
                context.getTestController().setTargetDuration(5400);
            } else {
                context.getTestController().setTargetDuration(3600);
            }

            logger.info("试验创建成功: {}", testId);

            closeWindow();
        } else {
            showAlert("创建试验失败，请检查数据库");
        }
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}