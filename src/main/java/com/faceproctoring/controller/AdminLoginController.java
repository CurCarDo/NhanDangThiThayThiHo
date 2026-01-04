package com.faceproctoring.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError;

    @FXML
    public void onLogin(ActionEvent e) {
        try {
            // normalize and trim input to avoid failed matches from whitespace/case
            String user = txtUser != null && txtUser.getText() != null ? txtUser.getText().trim() : "";
            String pass = txtPass != null && txtPass.getText() != null ? txtPass.getText().trim() : "";

            if ("admin".equalsIgnoreCase(user) && "123456".equals(pass)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                if (lblError != null) lblError.setText("Sai tài khoản hoặc mật khẩu!");
            }
        } catch (Exception ex) {
            if (lblError != null) lblError.setText("Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void onBack(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_select.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            if (lblError != null) lblError.setText("Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
