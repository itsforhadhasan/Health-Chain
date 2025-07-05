package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DoctorProfileController {

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorEmailLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label dobLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private Label licenseLabel;

    @FXML
    private Label experienceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label emergencyContactLabel;

    @FXML
    private Label workingDaysLabel;

    @FXML
    private Label workingHoursLabel;

    @FXML
    private Label consultationTimeLabel;

    @FXML
    private ImageView profileImageView;

    private UserData currentUser;

    @FXML
    public void initialize() {
        // Initialize profile with default values
        loadProfileData();
    }

    public void setUser(UserData user) {
        this.currentUser = user;
        updateUserInfo();
        loadProfileData();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            doctorNameLabel.setText("Dr. " + currentUser.fullName());
            doctorEmailLabel.setText(currentUser.email());
        }
    }

    private void loadProfileData() {
        if (currentUser != null) {
            // Load actual user data
            fullNameLabel.setText("Dr. " + currentUser.fullName());
            emailLabel.setText(currentUser.email());
            phoneLabel.setText(currentUser.phoneNumber() != null ? currentUser.phoneNumber() : "Not provided");
            
            // TODO: Load additional doctor-specific data from database
            // For now, using sample data
            dobLabel.setText("January 15, 1985");
            specializationLabel.setText("Cardiology");
            licenseLabel.setText("MD123456");
            experienceLabel.setText("15 years");
            statusLabel.setText("Active");
            addressLabel.setText("123 Medical Center Dr, Suite 456");
            cityLabel.setText("New York, NY 10001");
            emergencyContactLabel.setText("+1 (555) 987-6543");
            workingDaysLabel.setText("Monday - Friday");
            workingHoursLabel.setText("9:00 AM - 5:00 PM");
            consultationTimeLabel.setText("30 minutes");
        }
    }

    @FXML
    void handleDashboard() {
        try {
            FXMLScene.switchScene(FXMLScene.load("doctor_dashboard.fxml"), doctorNameLabel);
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to dashboard: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handlePatients() {
        try {
            // TODO: Navigate to patients page
            CustomAlertBox.showCustomAlert("Patients page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to patients page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleAppointments() {
        try {
            // TODO: Navigate to appointments page
            CustomAlertBox.showCustomAlert("Appointments page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to appointments page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleSchedule() {
        try {
            // TODO: Navigate to schedule page
            CustomAlertBox.showCustomAlert("Schedule page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to schedule page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleReports() {
        try {
            // TODO: Navigate to reports page
            CustomAlertBox.showCustomAlert("Reports page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to reports page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleSettings() {
        try {
            // TODO: Navigate to settings page
            CustomAlertBox.showCustomAlert("Settings page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to settings page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleEditProfile() {
        try {
            // TODO: Open edit profile dialog
            CustomAlertBox.showCustomAlert("Edit profile feature coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error opening edit profile dialog: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleChangePhoto() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Photo");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (selectedFile != null) {
                // Save the image to a user-specific location
                String destDir = System.getProperty("user.home") + File.separator + "HealthChainProfilePhotos";
                new File(destDir).mkdirs();
                String destPath = destDir + File.separator + (currentUser != null ? currentUser.id() : "doctor") + "_profile.png";
                Files.copy(selectedFile.toPath(), new File(destPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Update the ImageView
                profileImageView.setImage(new Image(new FileInputStream(destPath)));
                CustomAlertBox.showCustomAlert("Profile photo updated!", "success");
            }
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error updating photo: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), doctorNameLabel);
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error during logout: " + e.getMessage(), "error");
        }
    }
} 