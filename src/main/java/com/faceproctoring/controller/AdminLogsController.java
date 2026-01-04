package com.faceproctoring.controller;

import com.faceproctoring.model.RecognitionLog;
import com.faceproctoring.util.LogManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminLogsController {

    @FXML
    private TableView<RecognitionLog> tableRecognitionLogs;
    @FXML
    private TableColumn<RecognitionLog, String> colStudentId;
    @FXML
    private TableColumn<RecognitionLog, String> colFullName;
    @FXML
    private TableColumn<RecognitionLog, String> colClassName;
    @FXML
    private TableColumn<RecognitionLog, String> colRoom;
    @FXML
    private TableColumn<RecognitionLog, String> colStatus;
    @FXML
    private TableColumn<RecognitionLog, Double> colPercentage;
    @FXML
    private TableColumn<RecognitionLog, String> colRecognizedPerson;
    @FXML
    private TableColumn<RecognitionLog, String> colTime;

    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblTotalCount;
    @FXML
    private Label lblValidCount;
    @FXML
    private Label lblSuspiciousCount;
    @FXML
    private Label lblInvalidCount;

    private ObservableList<RecognitionLog> allLogs = FXCollections.observableArrayList();
    private ObservableList<RecognitionLog> filteredLogs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadLogs();
        updateStatistics();
    }

    private void setupTableColumns() {
        // Setup basic columns with property value factories
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
        colRecognizedPerson.setCellValueFactory(new PropertyValueFactory<>("recognizedPerson"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("recognitionTime"));

        // Setup percentage column with formatting
        colPercentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        colPercentage.setCellFactory(column -> new TableCell<RecognitionLog, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", percentage));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        // Setup status column with colored badges
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<RecognitionLog, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setStyle(getStatusBadgeStyle(status));
                    badge.setPrefWidth(130);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Center align all columns
        colStudentId.setStyle("-fx-alignment: CENTER;");
        colClassName.setStyle("-fx-alignment: CENTER;");
        colRoom.setStyle("-fx-alignment: CENTER;");
        colPercentage.setStyle("-fx-alignment: CENTER;");
        colStatus.setStyle("-fx-alignment: CENTER;");
        colTime.setStyle("-fx-alignment: CENTER;");
    }

    private String getStatusBadgeStyle(String status) {
        String baseStyle = "-fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 11;";
        switch (status) {
            case "HỢP LỆ":
                return baseStyle + "-fx-background-color: #e8f5e9; -fx-text-fill: #43a047;";
            case "NGHI NGỜ":
                return baseStyle + "-fx-background-color: #fff8e1; -fx-text-fill: #f9a825;";
            case "KHÔNG HỢP LỆ":
                return baseStyle + "-fx-background-color: #ffebee; -fx-text-fill: #e53935;";
            default:
                return baseStyle + "-fx-background-color: #f5f5f5; -fx-text-fill: #666;";
        }
    }

    private void loadLogs() {
        try {
            List<RecognitionLog> logs = LogManager.getInstance().getAllLogs();
            allLogs.clear();
            allLogs.addAll(logs);
            filteredLogs.clear();
            filteredLogs.addAll(logs);
            tableRecognitionLogs.setItems(filteredLogs);
            System.out.println("Loaded " + logs.size() + " recognition logs");
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu", "Không thể tải nhật ký xác thực: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = allLogs.size();
        int valid = (int) allLogs.stream().filter(log -> log.getStatus().equals("HỢP LỆ")).count();
        int suspicious = (int) allLogs.stream().filter(log -> log.getStatus().equals("NGHI NGỜ")).count();
        int invalid = (int) allLogs.stream().filter(log -> log.getStatus().equals("KHÔNG HỢP LỆ")).count();

        lblTotalCount.setText(String.valueOf(total));
        lblValidCount.setText(String.valueOf(valid));
        lblSuspiciousCount.setText(String.valueOf(suspicious));
        lblInvalidCount.setText(String.valueOf(invalid));
    }

    @FXML
    public void onSearch(ActionEvent event) {
        String searchText = txtSearch.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            filteredLogs.clear();
            filteredLogs.addAll(allLogs);
        } else {
            filteredLogs.clear();
            filteredLogs.addAll(allLogs.stream()
                    .filter(log -> log.getStudentId().toLowerCase().contains(searchText) ||
                            log.getFullName().toLowerCase().contains(searchText) ||
                            log.getClassName().toLowerCase().contains(searchText) ||
                            log.getRoom().toLowerCase().contains(searchText))
                    .toList());
        }
        tableRecognitionLogs.setItems(filteredLogs);
    }

    @FXML
    public void onRefresh(ActionEvent event) {
        loadLogs();
        updateStatistics();
        txtSearch.clear();
        showInfo("Làm mới thành công", "Dữ liệu đã được cập nhật!");
    }

    @FXML
    public void onClearAll(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa tất cả nhật ký?");
        alert.setContentText("Hành động này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            LogManager.getInstance().clearLogs();
            loadLogs();
            updateStatistics();
            showInfo("Xóa thành công", "Tất cả nhật ký đã được xóa!");
        }
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Excel");
        fileChooser.setInitialFileName("recognition_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) tableRecognitionLogs.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportToCSV(file);
                showInfo("Xuất thành công", "Dữ liệu đã được xuất ra: " + file.getName());
            } catch (Exception e) {
                showError("Lỗi xuất file", "Không thể xuất dữ liệu: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("MSSV,Họ tên,Lớp,Phòng thi,Trạng thái,Độ khớp,Nhận diện,Giờ xác thực\n");

            // Write data
            for (RecognitionLog log : allLogs) {
                writer.write(String.format("%s,%s,%s,%s,%s,%.1f%%,%s,%s\n",
                        log.getStudentId(),
                        log.getFullName(),
                        log.getClassName(),
                        log.getRoom(),
                        log.getStatus(),
                        log.getPercentage(),
                        log.getRecognizedPerson(),
                        log.getRecognitionTime()));
            }
        }
    }

    @FXML
    public void onDashboard(ActionEvent event) {
        navigateTo(event, "/fxml/admin_dashboard.fxml");
    }

    @FXML
    public void onStudents(ActionEvent event) {
        navigateTo(event, "/fxml/admin_students.fxml");
    }

    @FXML
    public void onFaceDatabase(ActionEvent event) {
        navigateTo(event, "/fxml/admin_face_database.fxml");
    }

    @FXML
    public void onLogout(ActionEvent event) {
        navigateTo(event, "/fxml/role_select.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi điều hướng", "Không thể mở trang: " + fxmlPath);
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
