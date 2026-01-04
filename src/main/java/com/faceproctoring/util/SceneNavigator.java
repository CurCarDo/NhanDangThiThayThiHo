package com.faceproctoring.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    private static Stage mainStage;

    // gọi trong Main.java khi khởi động app
    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ Lỗi khi chuyển scene: " + e.getMessage());
        }
    }
}
