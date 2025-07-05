package com.aoop.healthchain;

import com.aoop.healthchain.model.Appointment;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DoctorScheduleController implements Initializable {
    @FXML private TableView<Appointment> scheduleTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> purposeColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;

    private ObservableList<Appointment> schedule = FXCollections.observableArrayList();
    private UserData currentDoctor;

    public void setDoctor(UserData doctor) {
        this.currentDoctor = doctor;
        loadSchedule();
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
        scheduleTable.setItems(schedule);
    }

    private void loadSchedule() {
        schedule.clear();
        if (currentDoctor == null) return;
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String sql = "SELECT a.id, a.date, a.time, u.full_name as patient_name, a.department as purpose, a.status FROM appointments a JOIN users u ON a.patient_id = u.id WHERE a.doctor_id = ? AND a.date >= CURDATE() ORDER BY a.date ASC, a.time ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentDoctor.id().intValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                schedule.add(new Appointment(
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
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading schedule: " + e.getMessage(), "error");
        }
        scheduleTable.setItems(schedule);
    }
} 