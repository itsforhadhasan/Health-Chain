package com.aoop.healthchain;

import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.util.CustomAlertBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class DoctorRowController implements Initializable {
    @FXML private ImageView doctorAvatar;
    @FXML private Label doctorNameLabel;
    @FXML private Label doctorIdLabel;
    @FXML private Label doctorEmailLabel;
    @FXML private Label doctorPhoneLabel;
    @FXML private Label doctorSpecializationLabel;
    @FXML private Label doctorLicenseLabel;
    @FXML private Label statusBadge;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private DoctorData doctorData;
    private DoctorListController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        // Set button styles
        viewButton.getStyleClass().addAll("action-button", "view-button");
        editButton.getStyleClass().addAll("action-button", "edit-button");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
    }

    public void setDoctorData(DoctorData doctorData) {
        this.doctorData = doctorData;
        updateDisplay();
    }

    public void setParentController(DoctorListController parentController) {
        this.parentController = parentController;
    }

    private void updateDisplay() {
        if (doctorData != null) {
            doctorNameLabel.setText("Dr. " + doctorData.name());
            doctorIdLabel.setText("#" + String.format("%03d", doctorData.id()));
            doctorEmailLabel.setText(doctorData.email());
            doctorPhoneLabel.setText(doctorData.phone());
            doctorSpecializationLabel.setText(doctorData.specialization());
            doctorLicenseLabel.setText("License: " + doctorData.licenseNumber());
            statusBadge.setText(doctorData.status());

            // Set status badge color
            updateStatusBadgeStyle(doctorData.status());

            // Set avatar based on gender or use default
            setDoctorAvatar();
        }
    }

    private void updateStatusBadgeStyle(String status) {
        String baseStyle = "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: white;";
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                statusBadge.setStyle(baseStyle + " -fx-background-color: #10b981;");
                break;
            case "INACTIVE":
                statusBadge.setStyle(baseStyle + " -fx-background-color: #6b7280;");
                break;
            case "ON LEAVE":
                statusBadge.setStyle(baseStyle + " -fx-background-color: #f59e0b;");
                break;
            default:
                statusBadge.setStyle(baseStyle + " -fx-background-color: #6b7280;");
                break;
        }
    }

    private void setDoctorAvatar() {
        // For now, use default male avatar
        // In the future, you can check doctor's gender or profile picture
        try {
            Image avatarImage = new Image(getClass().getResourceAsStream("images/default_male.png"));
            doctorAvatar.setImage(avatarImage);
        } catch (Exception e) {
            // If image not found, keep the default from FXML
        }
    }

    @FXML
    private void handleView() {
        if (doctorData != null && parentController != null) {
            parentController.viewDoctorDetails(doctorData);
        }
    }

    @FXML
    private void handleEdit() {
        if (doctorData != null && parentController != null) {
            parentController.editDoctor(doctorData);
        }
    }

    @FXML
    private void handleDelete() {
        if (doctorData != null && parentController != null) {
            parentController.deleteDoctor(doctorData);
        }
    }
} 