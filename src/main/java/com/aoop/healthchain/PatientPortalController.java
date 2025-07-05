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

public class PatientPortalController implements Initializable {
    @FXML private Label patientNameLabel;
    @FXML private Label patientEmailLabel;
    @FXML private StackPane mainContent;

    private UserData currentUser;

    public void setUser(UserData user) {
        this.currentUser = user;
        patientNameLabel.setText(user.fullName());
        patientEmailLabel.setText(user.email());
        loadProfile();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Optionally load profile or leave blank
    }

    @FXML
    private void handleProfile() {
        loadProfile();
    }

    @FXML
    private void handleLogout() {
        // Implement logout logic as needed
    }

    @FXML
    private void handleAppointments() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("patient_appointments.fxml");
            PatientAppointmentController controller = (PatientAppointmentController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            setMainContent(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("patient_profile.fxml");
            PatientProfileController controller = (PatientProfileController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setUser(currentUser);
            }
            setMainContent(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMainContent(Node node) {
        mainContent.getChildren().setAll(node);
    }
} 