package com.faceproctoring.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class RoleSelectController implements Initializable {

    @FXML
    private ImageView studentIcon;

    @FXML
    private ImageView giamthiIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Try to load role icons, fall back to huit-logo.png when missing
        studentIcon.setImage(loadIconWithFallback("/images/student.png", "/images/huit-logo.png"));
        giamthiIcon.setImage(loadIconWithFallback("/images/giamthi.png", "/images/huit-logo.png"));
    }

    private Image loadIconWithFallback(String primaryPath, String fallbackPath) {
        URL res = getClass().getResource(primaryPath);
        if (res != null) {
            return new Image(res.toExternalForm());
        }
        URL fb = getClass().getResource(fallbackPath);
        if (fb != null) {
            return new Image(fb.toExternalForm());
        }
        return null;
    }

    public void onStudent(ActionEvent e) throws Exception {
        Stage stage = (Stage) ((javafx.scene.Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/student_login.fxml"))));
    }

    public void onAdmin(ActionEvent e) throws Exception {
        Stage stage = (Stage) ((javafx.scene.Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/admin_login.fxml"))));
    }
}
