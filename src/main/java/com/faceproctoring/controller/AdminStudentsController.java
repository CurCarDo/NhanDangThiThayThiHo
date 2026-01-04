package com.faceproctoring.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AdminStudentsController {
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<StudentRow> table;
    @FXML
    private TableColumn<StudentRow, String> colId;
    @FXML
    private TableColumn<StudentRow, String> colName;
    @FXML
    private TableColumn<StudentRow, String> colClass;
    @FXML
    private TableColumn<StudentRow, String> colRoom;
    @FXML
    private TableColumn<StudentRow, String> colStatus;
    @FXML
    private TableColumn<StudentRow, String> colMatch;
    @FXML
    private TableColumn<StudentRow, String> colTime;
    @FXML
    private TableColumn<StudentRow, Void> colAction;

    private final ObservableList<StudentRow> master = FXCollections.observableArrayList();

    public static class StudentRow {
        public final String id, name, clazz, room, status, match, time;

        public StudentRow(String id, String name, String clazz, String room, String status, String match, String time) {
            this.id = id;
            this.name = name;
            this.clazz = clazz;
            this.room = room;
            this.status = status;
            this.match = match;
            this.time = time;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getClazz() {
            return clazz;
        }

        public String getRoom() {
            return room;
        }

        public String getStatus() {
            return status;
        }

        public String getMatch() {
            return match;
        }

        public String getTime() {
            return time;
        }
    }

    @FXML
    public void initialize() {
        // Columns
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colClass.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getClazz()));
        colRoom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRoom()));
        colTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTime()));

        // Status badge
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Button b = new Button(item);
                b.setStyle("-fx-background-radius: 12; -fx-padding: 4 10; -fx-font-size: 12; -fx-cursor: hand;" +
                        (item.contains("Đã xác thực") ? "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;"
                                : item.contains("Nghi ngờ") ? "-fx-background-color: #fff8e1; -fx-text-fill: #f9a825;"
                                        : "-fx-background-color: #ffebee; -fx-text-fill: #e53935;"));
                setGraphic(b);
                setText(null);
            }
        });
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        // Match percent colored
        colMatch.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                try {
                    String v = item.replace("%", "").trim();
                    double d = Double.parseDouble(v);
                    setTextFill(d >= 90 ? Color.web("#43a047") : d >= 70 ? Color.web("#f9a825") : Color.web("#e53935"));
                } catch (Exception ignored) {
                    setTextFill(Color.BLACK);
                }
            }
        });
        colMatch.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMatch()));

        // Action column - view icon
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("👁");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Sample data
        master.addAll(
                new StudentRow("2021600123", "Nguyễn Văn A", "DHTH16A", "A101", "Đã xác thực", "96.8%", "14:35:22"),
                new StudentRow("2021600124", "Nguyễn Thị Kim Đoan", "DHTH16A", "A101", "Đã xác thực", "94.2%",
                        "14:34:15"),
                new StudentRow("2021600125", "Lê Văn C", "DHTH16B", "A102", "Nghi ngờ", "67.5%", "14:33:08"),
                new StudentRow("2021600126", "Phạm Thị D", "DHTH16B", "A102", "Thất bại", "42.3%", "14:32:45"),
                new StudentRow("2021600127", "Hoàng Văn E", "DHTH16C", "A103", "Đã xác thực", "98.1%", "14:31:30"),
                new StudentRow("2021600128", "Vũ Thị F", "DHTH16C", "A103", "Đã xác thực", "95.7%", "14:30:12"),
                new StudentRow("2021600129", "Đặng Văn G", "DHTH16A", "A101", "Nghi ngờ", "71.2%", "14:29:45"),
                new StudentRow("2021600130", "Bùi Thị H", "DHTH16B", "A102", "Đã xác thực", "93.4%", "14:28:33"));

        table.setItems(master);

        // Search filter
        txtSearch.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.toLowerCase();
            ObservableList<StudentRow> filtered = FXCollections.observableArrayList();
            for (StudentRow s : master) {
                if (s.id.toLowerCase().contains(q) || s.name.toLowerCase().contains(q)
                        || s.clazz.toLowerCase().contains(q)) {
                    filtered.add(s);
                }
            }
            table.setItems(filtered);
        });
    }

    @FXML
    private void onDashboard(ActionEvent e) {
        navigate(e, "/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void onFaceDatabase(ActionEvent e) {
        navigate(e, "/fxml/admin_face_database.fxml");
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
