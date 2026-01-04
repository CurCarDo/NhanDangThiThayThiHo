package com.faceproctoring.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.faceproctoring.util.SceneNavigator;

public class StudentLoginController {

    // ⚡ KHỚP VỚI FXML (fx:id trong student_login.fxml)
    @FXML
    private TextField txtStudentId;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblError;

    private final String API_BASE = "http://127.0.0.1:5000"; // Flask backend
    public static String TOKEN = "";
    public static String USERNAME = "";
    public static String FULLNAME = "";

    @FXML
    private void onLoginClicked() {
        try {
            // ⚡ Đúng biến FXML
            String username = txtStudentId.getText().trim();
            String password = txtPassword.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                lblError.setText("⚠️ Vui lòng nhập đầy đủ thông tin");
                return;
            }

            // Gửi request đăng nhập đến Flask API
            HttpClient client = HttpClient.newHttpClient();
            String json = new Gson().toJson(new LoginRequest(username, password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON trả về từ Flask
                LoginResponse data = new Gson().fromJson(response.body(), LoginResponse.class);
                TOKEN = data.token;
                USERNAME = data.user.username;
                FULLNAME = data.user.fullname; // ✅ thêm dòng này
                System.out.println("TOKEN = " + TOKEN);

                lblError.setText("✅ Đăng nhập thành công!");

                // Chuyển sang màn hình chính (nhận diện khuôn mặt)
                SceneNavigator.switchTo("/fxml/face_recognition.fxml");
            } else {
                lblError.setText("❌ Sai tài khoản hoặc mật khẩu");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("❌ Lỗi khi kết nối server");
        }
    }

    @FXML
    private void onBack() {
        // Quay lại màn hình chọn vai trò
        SceneNavigator.switchTo("/fxml/role_select.fxml");
    }

    // === INNER CLASSES ===
    private static class LoginRequest {
        String username, password;

        LoginRequest(String u, String p) {
            username = u;
            password = p;
        }
    }

    private static class LoginResponse {
        String token;
        User user;

        static class User {
            String username;
            String fullname;
            String role;
        }
    }
}
