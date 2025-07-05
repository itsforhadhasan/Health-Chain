package com.aoop.healthchain;

import com.aoop.healthchain.model.Appointment;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
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

public class DoctorAppointmentsController implements Initializable {
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> purposeColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void> actionsColumn;

    private ObservableList<Appointment> doctorAppointments = FXCollections.observableArrayList();
    private UserData currentDoctor;
    private DoctorPortalController parentController;

    public void setDoctor(UserData doctor) {
        this.currentDoctor = doctor;
        loadDoctorAppointments();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        patientColumn.setCellValueFactory(cellData -> cellData.getValue().doctorProperty()); // doctorProperty used for patient name here
        purposeColumn.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        actionsColumn.setCellFactory(getActionsCellFactory());
        appointmentsTable.setItems(doctorAppointments);
    }

    private Callback<TableColumn<Appointment, Void>, TableCell<Appointment, Void>> getActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirm");
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pane = new HBox(5, confirmBtn, completeBtn, cancelBtn);
            {
                confirmBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Confirmed"));
                completeBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Completed"));
                cancelBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Cancelled"));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private void loadDoctorAppointments() {
        doctorAppointments.clear();
        if (currentDoctor == null) return;
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String sql = "SELECT a.id, a.date, a.time, u.full_name as patient_name, a.department as purpose, a.status FROM appointments a JOIN users u ON a.patient_id = u.id WHERE a.doctor_id = (SELECT doctors.id FROM doctors WHERE doctors.user_id = ?) ORDER BY a.date DESC, a.time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            System.out.println(currentDoctor.id().intValue());
            stmt.setInt(1, currentDoctor.id().intValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                doctorAppointments.add(new Appointment(
                    rs.getInt("id"),
                    0, // patientId not needed here
                    currentDoctor.id().intValue(),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("patient_name"),
                    rs.getString("purpose"),
                    rs.getString("status")
                ));
            }
            System.out.println(doctorAppointments);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading appointments: " + e.getMessage(), "error");
        }
        appointmentsTable.setItems(doctorAppointments);
    }

    private void updateAppointmentStatus(Appointment appointment, String newStatus) {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String sql = "UPDATE appointments SET status = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, appointment.getId());
            stmt.executeUpdate();
            appointment.setStatus(newStatus);
            appointmentsTable.refresh();
            CustomAlertBox.showCustomAlert("Appointment status updated to " + newStatus, "success");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error updating status: " + e.getMessage(), "error");
        }
    }

    public void setParentController(DoctorPortalController parent) {
        this.parentController = parent;
    }

    public void goToPatients() {
        if (parentController != null) {
            parentController.handlePatients();
        }
    }
} 