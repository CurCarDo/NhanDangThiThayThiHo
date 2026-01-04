package com.faceproctoring.controller;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminDashboardController {
    @FXML
    public Label lblTotal, lblMatched, lblUnmatched;
    private Socket socket;

    // Thống kê đơn giản trong phiên
    private final AtomicInteger totalCount = new AtomicInteger(0);
    private final AtomicInteger matchedCount = new AtomicInteger(0);
    private final AtomicInteger unmatchedCount = new AtomicInteger(0);
    private final AtomicInteger suspectCount = new AtomicInteger(0);
    public void initialize() {
        System.out.println("AdminDashboard initialized.");
        connectSocket();
    }
    private void connectSocket() {
        try {
            socket = IO.socket("http://127.0.0.1:5000");
            socket.on("connect", args -> System.out.println("✅ Connected to Flask SocketIO"));
            socket.on("student_update", onStudentUpdate);
            socket.on("disconnect", args -> System.out.println("⚠️ Disconnected from Flask SocketIO"));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private final Emitter.Listener onStudentUpdate = args -> {
        Platform.runLater(() -> {
            try {
                JSONObject data = new JSONObject(args[0].toString());
                String name = data.optString("name", "Không rõ");
                String status = data.optString("status", "UNKNOWN");
                double score = data.optDouble("score", 0.0);

                totalCount.incrementAndGet();
                if (status.equalsIgnoreCase("HỢP LỆ")) matchedCount.incrementAndGet();
                else if (status.equalsIgnoreCase("KHÔNG HỢP LỆ")) unmatchedCount.incrementAndGet();
                else if (status.equalsIgnoreCase("NGHI NGỜ")) suspectCount.incrementAndGet();

                // Cập nhật dashboard
                lblTotal.setText(String.valueOf(totalCount.get()));
                lblMatched.setText(String.valueOf(matchedCount.get()));
                lblUnmatched.setText(String.valueOf(unmatchedCount.get()));

                System.out.printf("📡 [AdminDashboard] %s - %s (%.2f%%)%n",
                        name, status, score);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    };
    @FXML
    public void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_select.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onStudents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_students.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onFaceDatabase(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_face_database.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_logs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
