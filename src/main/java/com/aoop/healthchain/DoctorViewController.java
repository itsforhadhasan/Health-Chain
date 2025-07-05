package com.aoop.healthchain;

import com.aoop.healthchain.model.DoctorData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorViewController implements Initializable {
    @FXML private ImageView doctorAvatar;
    @FXML private Label doctorNameLabel;
    @FXML private Label doctorIdLabel;
    @FXML private Label doctorStatusLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label specializationLabel;
    @FXML private Label licenseLabel;
    @FXML private Label addressLabel;
    @FXML private Label cityLabel;
    @FXML private Label stateLabel;
    @FXML private Label zipLabel;
    @FXML private Label issueDateLabel;
    @FXML private Label expiryDateLabel;
    @FXML private Label hospitalLabel;

    private DoctorData doctorData;
    private DoctorListController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize any setup here
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
            emailLabel.setText(doctorData.email());
            phoneLabel.setText(doctorData.phone());
            specializationLabel.setText(doctorData.specialization());
            licenseLabel.setText(doctorData.licenseNumber());
            addressLabel.setText(doctorData.address());
            cityLabel.setText(doctorData.city());
            stateLabel.setText(doctorData.state());
            zipLabel.setText(doctorData.zip());
            
            // Format dates
            if (doctorData.issueDate() != null && !doctorData.issueDate().isEmpty()) {
                issueDateLabel.setText(formatDate(doctorData.issueDate()));
            }
            if (doctorData.expiryDate() != null && !doctorData.expiryDate().isEmpty()) {
                expiryDateLabel.setText(formatDate(doctorData.expiryDate()));
            }
            
            hospitalLabel.setText(doctorData.hospitalName() != null ? doctorData.hospitalName() : "Not Assigned");
            
            // Set status badge
            doctorStatusLabel.setText(doctorData.status());
            updateStatusBadgeStyle(doctorData.status());
            
            // Set avatar
            setDoctorAvatar();
        }
    }

    private String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }

    private void updateStatusBadgeStyle(String status) {
        String baseStyle = "-fx-padding: 6 16; -fx-background-radius: 16; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white;";
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                doctorStatusLabel.setStyle(baseStyle + " -fx-background-color: #10b981;");
                break;
            case "INACTIVE":
                doctorStatusLabel.setStyle(baseStyle + " -fx-background-color: #6b7280;");
                break;
            case "ON LEAVE":
                doctorStatusLabel.setStyle(baseStyle + " -fx-background-color: #f59e0b;");
                break;
            case "PENDING":
                doctorStatusLabel.setStyle(baseStyle + " -fx-background-color: #8b5cf6;");
                break;
            default:
                doctorStatusLabel.setStyle(baseStyle + " -fx-background-color: #6b7280;");
                break;
        }
    }

    private void setDoctorAvatar() {
        try {
            Image avatarImage = new Image(getClass().getResourceAsStream("images/default_male.png"));
            doctorAvatar.setImage(avatarImage);
        } catch (Exception e) {
            // Keep default image if loading fails
        }
    }

    @FXML
    private void handleEdit() {
        if (doctorData != null && parentController != null) {
            // Close the view dialog
            closeDialog();
            
            // Open edit dialog
            parentController.editDoctor(doctorData);
        }
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) doctorNameLabel.getScene().getWindow();
        stage.close();
    }
} 