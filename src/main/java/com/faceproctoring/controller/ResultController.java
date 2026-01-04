package com.faceproctoring.controller;

import com.faceproctoring.model.RecognitionLog;
import com.faceproctoring.util.LogManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ResultController {

    @FXML
    private Label lblStudentId;
    @FXML
    private Label lblStudentName;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblPercent;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblMatchedPerson;
    @FXML
    private StackPane progressFill;
    @FXML
    private ImageView imgCaptured;

    @FXML
    private Label lblVerificationWarning;
    @FXML
    private Label lblWarning; // ⚠️ Đã gắn với label cảnh báo Flask trong FXML

    private String currentClassName;
    private String currentRoom;

    private static final double PROGRESS_BAR_WIDTH = 520.0;

    public void initialize() {
        // Remove hardcoded test data that was causing persistent log entries
        // setResultData(...); // This line is removed
    }

    public void setResultData(String studentId, String studentName, String date, String time, double matchRatio) {
        setResultData(studentId, studentName, date, time, matchRatio, "N/A", "N/A", "gray");
    }

    public void setResultData(String studentId, String studentName, String date, String time,
            double matchRatio, String status, String matchedPerson, String colorName) {
        setResultData(studentId, studentName, date, time, matchRatio, status, matchedPerson, colorName, "", "", "");
    }

    public void setResultData(String studentId, String studentName, String date, String time,
            double matchRatio, String status, String matchedPerson,
            String colorName, String className, String room) {
        setResultData(studentId, studentName, date, time, matchRatio,
                status, matchedPerson, colorName, className, room, "");
    }

    // 🔥 Bản đầy đủ — hiển thị kết luận & cảnh báo từ Flask + Java
    public void setResultData(String studentId, String studentName, String date, String time,
            double matchRatio, String status, String matchedPerson,
            String colorName, String className, String room, String warningMessage) {

        this.currentClassName = className;
        this.currentRoom = room;

        // ===== Thông tin sinh viên =====
        if (lblStudentId != null)
            lblStudentId.setText(studentId);
        if (lblStudentName != null)
            lblStudentName.setText(studentName);
        if (lblDate != null)
            lblDate.setText(date);
        if (lblTime != null)
            lblTime.setText(time);
        if (lblPercent != null)
            lblPercent.setText(String.format("%.1f%%", matchRatio * 100));

        // ===== Trạng thái (HỢP LỆ / NGHI NGỜ / ...) =====
        if (lblStatus != null) {
            lblStatus.setText(status.toUpperCase());
            Color color = getColorFromName(colorName);
            lblStatus.setTextFill(color);
            lblStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 28px;");
        }

        // ===== Thông báo Flask (warningMessage) =====
        if (lblWarning != null) {
            if (warningMessage != null && !warningMessage.isEmpty()) {
                lblWarning.setVisible(true);
                lblWarning.setText(warningMessage);
                lblWarning.setWrapText(true);
                lblWarning.setStyle(
                        "-fx-font-size: 15px; -fx-font-style: italic; " +
                                "-fx-text-fill: " + getCSSColor(colorName) + "; " +
                                "-fx-padding: 8;");
            } else {
                lblWarning.setVisible(false);
                lblWarning.setText("");
            }
        }

        // ===== Người nhận diện được =====
        if (lblMatchedPerson != null) {
            lblMatchedPerson.setText("Nhận diện: " + (matchedPerson != null ? matchedPerson : "Không xác định"));
        }

        // ===== Thanh tiến trình độ khớp =====
        if (progressFill != null) {
            double width = Math.max(0, Math.min(1.0, matchRatio)) * PROGRESS_BAR_WIDTH;
            progressFill.setMaxWidth(width);
            progressFill.setPrefWidth(width);
            String cssColor = getCSSColor(colorName);
            progressFill.setStyle("-fx-background-color: " + cssColor + "; -fx-background-radius: 10;");
        }

        // ===== Lưu log =====
        saveLog(studentId, studentName, className, room, status, matchRatio, matchedPerson);
    }

    private void saveLog(String studentId, String fullName, String className, String room,
            String status, double percentage, String recognizedPerson) {
        try {
            RecognitionLog log = new RecognitionLog(
                    studentId,
                    fullName,
                    className != null ? className : "N/A",
                    room != null ? room : "N/A",
                    status,
                    percentage * 100,
                    recognizedPerson);
            LogManager.getInstance().addLog(log);
            System.out.println("Recognition log saved for: " + studentId);
        } catch (Exception e) {
            System.err.println("Error saving recognition log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Hiển thị ảnh =====
    public void setCapturedImage(Image image) {
        if (imgCaptured != null && image != null) {
            imgCaptured.setImage(image);
        }
    }

    // ===== Hiển thị kết luận xác minh (từ Java logic) =====
    public void setVerificationStatus(boolean isVerified, String message) {
        if (lblVerificationWarning != null && message != null && !message.isEmpty()) {
            lblVerificationWarning.setText(message);
            lblVerificationWarning.setVisible(true);
            lblVerificationWarning.setWrapText(true);
            if (isVerified) {
                lblVerificationWarning.setStyle(
                        "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; " +
                                "-fx-padding: 15; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 16px;");
            } else {
                lblVerificationWarning.setStyle(
                        "-fx-background-color: #ffebee; -fx-text-fill: #c62828; " +
                                "-fx-padding: 15; -fx-background-radius: 8; -fx-font-weight: bold; " +
                                "-fx-font-size: 16px; -fx-border-color: #c62828; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        } else if (lblVerificationWarning != null) {
            lblVerificationWarning.setVisible(false);
        }
    }

    // ===== Màu JavaFX và CSS =====
    private Color getColorFromName(String colorName) {
        switch (colorName.toLowerCase()) {
            case "green":
                return Color.web("#4CAF50");
            case "orange":
                return Color.web("#FF9800");
            case "red":
                return Color.web("#F44336");
            default:
                return Color.GRAY;
        }
    }

    private String getCSSColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "green":
                return "#4CAF50";
            case "orange":
                return "#FF9800";
            case "red":
                return "#F44336";
            default:
                return "#9E9E9E";
        }
    }

    @FXML
    private void handleContinueToExam(ActionEvent event) {
        try {
            // Load the role_select.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/role_select.fxml"));

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error navigating to role_select.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
