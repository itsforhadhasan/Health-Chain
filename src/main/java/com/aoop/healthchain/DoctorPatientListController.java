package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DoctorPatientListController implements Initializable {
    @FXML private TableView<UserData> patientsTable;
    @FXML private TableColumn<UserData, String> nameColumn;
    @FXML private TableColumn<UserData, String> emailColumn;
    @FXML private TableColumn<UserData, String> phoneColumn;
    @FXML private TableColumn<UserData, String> statusColumn;
    @FXML private TableColumn<UserData, Void> actionsColumn;

    private ObservableList<UserData> myPatients = FXCollections.observableArrayList();
    private Long doctorId;
    private DoctorPortalController parentController;

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
        loadPatientsForDoctor();
    }

    public void setParentController(DoctorPortalController parent) {
        this.parentController = parent;
    }

    public void goToAppointments() {
        if (parentController != null) {
            parentController.handleAppointments();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().fullName()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().email()));
        phoneColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().phoneNumber()));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isVerified() ? "Active" : "Inactive"));
        actionsColumn.setCellFactory(getActionsCellFactory());
        patientsTable.setItems(myPatients);
    }

    private Callback<TableColumn<UserData, Void>, TableCell<UserData, Void>> getActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("View Profile");
            private final HBox pane = new HBox(5, viewBtn);
            {
                viewBtn.setOnAction(e -> {
                    UserData patient = getTableView().getItems().get(getIndex());
                    showPatientProfile(patient);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private void loadPatientsForDoctor() {
        myPatients.clear();
        if (doctorId == null) return;
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String sql = "SELECT DISTINCT u.id, u.email, u.full_name, u.phone, '' as blood_group, '' as password, u.role, 1 as is_verified FROM appointments a JOIN users u ON a.patient_id = u.id WHERE a.doctor_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                myPatients.add(new UserData(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("blood_group"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getInt("is_verified") == 1
                ));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading patients: " + e.getMessage(), "error");
        }
    }

    private void showPatientProfile(UserData patient) {
        try {
            FXMLScene fxmlScene = FXMLScene.load("patient_profile.fxml");
            PatientProfileController controller = (PatientProfileController) fxmlScene.getController();
            if (controller != null) {
                controller.setUser(patient);
            }
            patientsTable.getScene().setRoot(fxmlScene.getRoot());
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error loading patient profile: " + e.getMessage(), "error");
        }
    }
} 