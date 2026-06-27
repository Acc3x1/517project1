package com.iso11820.ui;

import com.iso11820.GlobalContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 记录查询界面控制器
 */
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private TextField txtProductId;
    @FXML private ComboBox<String> cmbOperator;
    @FXML private Button btnQuery;
    @FXML private Button btnExport;

    @FXML private TableView<TestRecord> tableRecords;
    @FXML private TableColumn<TestRecord, String> colTestId;
    @FXML private TableColumn<TestRecord, String> colProductId;
    @FXML private TableColumn<TestRecord, String> colDate;
    @FXML private TableColumn<TestRecord, String> colOperator;
    @FXML private TableColumn<TestRecord, Integer> colDuration;
    @FXML private TableColumn<TestRecord, String> colPostWeight;
    @FXML private TableColumn<TestRecord, String> colLostRate;
    @FXML private TableColumn<TestRecord, String> colStatus;

    private final GlobalContext context = GlobalContext.getInstance();
    private ObservableList<TestRecord> records = FXCollections.observableArrayList();

    // 保存原始查询结果用于导出
    private List<Object[]> lastQueryResults;

    @FXML
    public void initialize() {
        // 设置日期范围
        dpFromDate.setValue(LocalDate.now().minusMonths(1));
        dpToDate.setValue(LocalDate.now());

        // 设置操作员选项
        cmbOperator.getItems().addAll("全部", "admin", "experimenter");
        cmbOperator.setValue("全部");

        // 设置表格列
        colTestId.setCellValueFactory(new PropertyValueFactory<>("testId"));
        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOperator.setCellValueFactory(new PropertyValueFactory<>("operator"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colPostWeight.setCellValueFactory(new PropertyValueFactory<>("postWeight"));
        colLostRate.setCellValueFactory(new PropertyValueFactory<>("lostRate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableRecords.setItems(records);

        // 双击查看详情
        tableRecords.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showDetail();
            }
        });

        logger.info("记录查询界面初始化");
    }

    @FXML
    public void onQuery() {
        String fromDate = dpFromDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String toDate = dpToDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String productId = txtProductId.getText().trim();

        // 获取操作员筛选条件 — "全部"传空字符串，否则传具体操作员名
        String operatorFilter = cmbOperator.getValue();
        if ("全部".equals(operatorFilter)) {
            operatorFilter = "";
        }

        List<Object[]> results = context.getDbHelper().queryTests(fromDate, toDate, productId, operatorFilter);

        // 保存原始结果用于导出
        lastQueryResults = results;

        records.clear();
        for (Object[] row : results) {
            String status = "10000000".equals((String) row[5]) ? "已保存" : "未保存";
            double postWeight = (Double) row[7];
            double lostPer = (Double) row[8];
            String postWeightStr = postWeight > 0 ? String.format("%.2f g", postWeight) : "-";
            String lostRateStr = postWeight > 0 ? String.format("%.2f%%", lostPer) : "-";
            records.add(new TestRecord(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (Integer) row[4],
                status,
                postWeightStr,
                lostRateStr
            ));
        }

        logger.info("查询到 {} 条记录", records.size());
    }

    @FXML
    public void onExport() {
        if (lastQueryResults == null || lastQueryResults.isEmpty()) {
            showAlert("没有查询结果可导出，请先查询");
            return;
        }

        // 生成导出文件路径
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String basePath = context.getConfig().getReport().getOutputDirectory();
        String filePath = basePath + "/" + timestamp + "_查询结果.xlsx";

        String result = context.getExportService().exportQueryExcel(lastQueryResults, filePath);

        if (result != null) {
            showAlert("导出成功\n文件已保存到: " + result);
        } else {
            showAlert("导出失败，请检查文件路径");
        }
    }

    private void showDetail() {
        TestRecord selected = tableRecords.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Object[] detail = context.getDbHelper().queryTestDetail(selected.getProductId(), selected.getTestId());
        if (detail != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("样品编号: ").append(detail[0]).append("\n");
            sb.append("试验编号: ").append(detail[1]).append("\n");
            sb.append("试验日期: ").append(detail[2]).append("\n");
            sb.append("操作员: ").append(detail[3]).append("\n");
            sb.append("环境温度: ").append(detail[4]).append(" °C\n");
            sb.append("环境湿度: ").append(detail[5]).append(" %\n");
            sb.append("试验前质量: ").append(detail[8]).append(" g\n");
            sb.append("试验后质量: ").append(detail[9]).append(" g\n");
            sb.append("失重率: ").append(String.format("%.2f", (Double) detail[10])).append(" %\n");
            sb.append("温升: ").append(String.format("%.2f", (Double) detail[11])).append(" °C\n");
            sb.append("试验时长: ").append(detail[12]).append(" 秒\n");

            // 火焰信息
            String phenocode = (String) detail[13];
            if (phenocode != null && !phenocode.isEmpty()) {
                sb.append("火焰现象: 有持续火焰\n");
                sb.append("火焰发生时刻: ").append(detail[14]).append(" 秒\n");
                sb.append("火焰持续时间: ").append(detail[15]).append(" 秒\n");
            } else {
                sb.append("火焰现象: 无\n");
            }

            // 判定结论
            double deltaTf = (Double) detail[11];
            double lostPer = (Double) detail[10];
            int flameDur = (Integer) detail[15];
            boolean passed = deltaTf <= 50 && lostPer <= 50 && flameDur < 5;
            sb.append("\n综合判定: ").append(passed ? "合格" : "不合格").append("\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("试验详情");
            alert.setHeaderText(selected.getTestId());
            alert.setContentText(sb.toString());
            alert.showAndWait();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 表格数据模型
    public static class TestRecord {
        private String testId;
        private String productId;
        private String date;
        private String operator;
        private int duration;
        private String status;
        private String postWeight;
        private String lostRate;

        public TestRecord(String testId, String productId, String date, String operator,
                         int duration, String status, String postWeight, String lostRate) {
            this.testId = testId;
            this.productId = productId;
            this.date = date;
            this.operator = operator;
            this.duration = duration;
            this.status = status;
            this.postWeight = postWeight;
            this.lostRate = lostRate;
        }

        public String getTestId() { return testId; }
        public String getProductId() { return productId; }
        public String getDate() { return date; }
        public String getOperator() { return operator; }
        public int getDuration() { return duration; }
        public String getStatus() { return status; }
        public String getPostWeight() { return postWeight; }
        public String getLostRate() { return lostRate; }
    }
}
