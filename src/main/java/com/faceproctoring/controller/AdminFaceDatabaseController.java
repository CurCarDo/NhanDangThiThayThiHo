package com.faceproctoring.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class AdminFaceDatabaseController {
    @FXML
    private TextField txtSearch;
    @FXML
    private FlowPane cardContainer;

    private final List<FaceData> masterData = new ArrayList<>();

    public static class FaceData {
        public final String id, name, clazz, photoCount, lastUpdate, status;

        public FaceData(String id, String name, String clazz, String photoCount, String lastUpdate, String status) {
            this.id = id;
            this.name = name;
            this.clazz = clazz;
            this.photoCount = photoCount;
            this.lastUpdate = lastUpdate;
            this.status = status;
        }
    }

    @FXML
    public void initialize() {
        // Sample data
        masterData.add(new FaceData("2021600123", "Nguyễn Văn A", "DHTH16A", "5 ảnh", "15/03/2024", "Đã train"));
        masterData.add(new FaceData("2021600124", "Nguyễn Thị Kim Đoan", "DHTH16A", "5 ảnh", "15/03/2024", "Đã train"));
        masterData.add(new FaceData("2021600125", "Lê Văn C", "DHTH16B", "3 ảnh", "14/03/2024", "Chờ train"));
        masterData.add(new FaceData("2021600126", "Phạm Thị D", "DHTH16B", "5 ảnh", "15/03/2024", "Đã train"));
        masterData.add(new FaceData("2021600127", "Hoàng Văn E", "DHTH16C", "4 ảnh", "14/03/2024", "Chờ train"));
        masterData.add(new FaceData("2021600128", "Vũ Thị F", "DHTH16C", "5 ảnh", "15/03/2024", "Đã train"));

        renderCards(masterData);

        // Search filter
        txtSearch.textProperty().addListener((obs, o, n) -> {
            String q = (n == null ? "" : n).toLowerCase();
            List<FaceData> filtered = new ArrayList<>();
            for (FaceData f : masterData) {
                if (f.id.toLowerCase().contains(q) || f.name.toLowerCase().contains(q)
                        || f.clazz.toLowerCase().contains(q)) {
                    filtered.add(f);
                }
            }
            renderCards(filtered);
        });
    }

    private void renderCards(List<FaceData> data) {
        cardContainer.getChildren().clear();
        for (FaceData f : data) {
            cardContainer.getChildren().add(createCard(f));
        }
    }

    private VBox createCard(FaceData data) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2);");
        card.setPrefWidth(360);
        card.setMaxWidth(360);

        // Top: Avatar + Name + ID + Badge
        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle
        StackPane avatar = new StackPane();
        Circle circle = new Circle(28);
        circle.setFill(Color.web("#e3f2fd"));
        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 24; -fx-text-fill: #1976d2;");
        avatar.getChildren().addAll(circle, icon);

        // Name + ID
        VBox nameBox = new VBox(2);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(data.name);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label lblId = new Label(data.id);
        lblId.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
        nameBox.getChildren().addAll(lblName, lblId);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // Status badge
        Button badge = new Button(data.status);
        badge.setStyle("-fx-background-radius: 12; -fx-padding: 4 10; -fx-font-size: 11; -fx-cursor: default;" +
                (data.status.equals("Đã train") ? "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;"
                        : "-fx-background-color: #fff8e1; -fx-text-fill: #f9a825;"));

        top.getChildren().addAll(avatar, nameBox, badge);

        // Info rows
        VBox info = new VBox(6);
        info.getChildren().addAll(
                createInfoRow("Lớp:", data.clazz),
                createInfoRow("Số ảnh:", data.photoCount),
                createInfoRow("Cập nhật:", data.lastUpdate));

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        Button btnView = new Button("Xem ảnh");
        btnView.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-weight: bold;");
        Button btnUpdate = new Button("Cập nhật");
        btnUpdate.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-weight: bold;");
        HBox.setHgrow(btnView, Priority.ALWAYS);
        HBox.setHgrow(btnUpdate, Priority.ALWAYS);
        btnView.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        actions.getChildren().addAll(btnView, btnUpdate);

        card.getChildren().addAll(top, new Separator(), info, actions);
        return card;
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    @FXML
    private void onDashboard(ActionEvent e) {
        navigate(e, "/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void onStudents(ActionEvent e) {
        navigate(e, "/fxml/admin_students.fxml");
    }

    @FXML
    private void onLogs(ActionEvent e) {
        navigate(e, "/fxml/admin_logs.fxml");
    }

    @FXML
    private void onLogout(ActionEvent e) {
        navigate(e, "/fxml/role_select.fxml");
    }

    private void navigate(ActionEvent e, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
