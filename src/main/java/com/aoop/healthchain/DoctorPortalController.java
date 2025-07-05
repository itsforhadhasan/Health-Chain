package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.FXMLScene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class DoctorPortalController implements Initializable {
    @FXML private Label doctorNameLabel;
    @FXML private Label doctorEmailLabel;
    @FXML private StackPane mainContent;

    private UserData currentUser;

    public void setUser(UserData user) {
        this.currentUser = user;
        doctorNameLabel.setText("Dr. " + user.fullName());
        doctorEmailLabel.setText(user.email());
        loadDashboard();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Optionally load dashboard or leave blank
    }

    @FXML
    private void handleDashboard() {
        loadDashboard();
    }

    public void handlePatients() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("doctor_patient_list.fxml");
            DoctorPatientListController controller = (DoctorPatientListController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setDoctorId(currentUser.id());
                controller.setParentController(this);
            }
            setMainContent(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleAppointments() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("doctor_appointments.fxml");
            DoctorAppointmentsController controller = (DoctorAppointmentsController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setDoctor(currentUser);
                controller.setParentController(this);
            }
            setMainContent(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSchedule() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("doctor_schedule.fxml");
            DoctorScheduleController controller = (DoctorScheduleController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setDoctor(currentUser);
            }
            setMainContent(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Implement logout logic as needed
    }

    private void loadDashboard() {
        // Optionally load a dashboard view or welcome message
        mainContent.getChildren().clear();
        Label welcome = new Label("Welcome to the Doctor Portal!");
        welcome.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
        mainContent.getChildren().add(welcome);
    }

    private void setMainContent(Node node) {
        mainContent.getChildren().setAll(node);
    }
} 