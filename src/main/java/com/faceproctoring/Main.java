package com.faceproctoring;

import com.faceproctoring.util.SceneNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ✅ Gán stage cho SceneNavigator để các controller có thể chuyển scene
        SceneNavigator.setStage(stage);

        // ✅ Load màn hình đầu tiên (chọn vai trò)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_select.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setMaximized(true); // ✅ Mở vừa màn hình
        stage.centerOnScreen();   // ✅ Căn giữa màn hình


        stage.setTitle("FaceProctoring - Nhận diện thi hộ");
        stage.setScene(scene);

        // ✅ Dọn tài nguyên khi đóng cửa sổ
        stage.setOnCloseRequest(event -> {
            System.out.println("🧹 Đang thoát ứng dụng, dọn tài nguyên...");
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
