package com.iso11820.ui;

import com.iso11820.GlobalContext;
import com.iso11820.db.DbHelper;
import com.iso11820.model.Operator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录界面控制器
 */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private RadioButton rbAdmin;
    @FXML private RadioButton rbExperimenter;
    @FXML private TextField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    private final GlobalContext context = GlobalContext.getInstance();

    @FXML
    public void initialize() {
        // 设置单选按钮组
        ToggleGroup group = new ToggleGroup();
        rbAdmin.setToggleGroup(group);
        rbExperimenter.setToggleGroup(group);
        rbAdmin.setSelected(true);

        logger.info("登录界面初始化");
    }

    @FXML
    public void onLogin() {
        String username = rbAdmin.isSelected() ? "admin" : "experimenter";
        String password = txtPassword.getText();

        if (password.isEmpty()) {
            lblMessage.setText("请输入密码");
            return;
        }

        DbHelper dbHelper = context.getDbHelper();
        String[] result = new String[2];

        if (dbHelper.login(username, password, result)) {
            // 登录成功
            Operator operator = new Operator(result[0], username, password, result[1]);
            context.setCurrentOperator(operator);

            logger.info("用户 {} 登录成功", username);

            // 打开主界面
            openMainWindow();
        } else {
            lblMessage.setText("密码错误，请重新输入");
            txtPassword.clear();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("ISO 11820 建筑材料不燃性试验仿真系统");
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

            // 关闭登录窗口
            Stage loginStage = (Stage) btnLogin.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            logger.error("打开主界面失败", e);
            lblMessage.setText("系统错误");
        }
    }
}