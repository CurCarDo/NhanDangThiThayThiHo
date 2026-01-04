package com.faceproctoring.controller;

import com.faceproctoring.util.StudentDataLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StudentInfoController {
    @FXML
    private Label lblStudentId;
    @FXML
    private Label lblStudentName;
    @FXML
    private Label lblClass;
    @FXML
    private Label lblRoom;

    public void initialize() {
        lblStudentId.setText("SV001");
        String fullName = StudentDataLoader.getFullName("SV001");
        String clazz = StudentDataLoader.getClazz("SV001");
        String room = StudentDataLoader.getRoom("SV001");
        lblStudentName.setText(fullName);
        lblClass.setText(clazz);
        lblRoom.setText(room);
    }

    public void onOpenCamera(ActionEvent e) throws Exception {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_recognition.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    public void onShowResult(ActionEvent e) throws Exception {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/result.fxml"));
        javafx.scene.Parent root = loader.load();
        ResultController controller = loader.getController();
        // Truyền dữ liệu mẫu, sau này thay bằng dữ liệu thực
        controller.setResultData(lblStudentId.getText(), lblStudentName.getText(), "15/03/2024", "14:35:22", 0.968,
                "HỢP LỆ", lblStudentName.getText(), "green", lblClass.getText(), lblRoom.getText());
        stage.setScene(new Scene(root));
    }
}
