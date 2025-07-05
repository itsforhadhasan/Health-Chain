package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PatientProfileController {
    @FXML private Label patientNameLabel;
    @FXML private Label patientEmailLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label dobLabel;
    @FXML private Label bloodGroupLabel;
    @FXML private Label statusLabel;
    @FXML private Label addressLabel;
    @FXML private ImageView profileImageView;

    private UserData currentUser;

    public void setUser(UserData user) {
        this.currentUser = user;
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            patientNameLabel.setText(currentUser.fullName());
            patientEmailLabel.setText(currentUser.email());
            fullNameLabel.setText(currentUser.fullName());
            emailLabel.setText(currentUser.email());
            phoneLabel.setText(currentUser.phoneNumber() != null ? currentUser.phoneNumber() : "Not provided");
            bloodGroupLabel.setText(currentUser.bloodGroup() != null ? currentUser.bloodGroup() : "Not provided");
            statusLabel.setText(currentUser.isVerified() ? "Active" : "Inactive");
            // dob and address would be loaded from DB if available
            dobLabel.setText("");
            addressLabel.setText("");
            // Load profile photo if exists
            String destPath = System.getProperty("user.home") + File.separator + "HealthChainProfilePhotos" + File.separator + currentUser.id() + "_profile.png";
            File photoFile = new File(destPath);
            if (photoFile.exists()) {
                try {
                    profileImageView.setImage(new Image(new FileInputStream(destPath)));
                } catch (Exception ignored) {}
            }
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
                String destDir = System.getProperty("user.home") + File.separator + "HealthChainProfilePhotos";
                new File(destDir).mkdirs();
                String destPath = destDir + File.separator + currentUser.id() + "_profile.png";
                Files.copy(selectedFile.toPath(), new File(destPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                profileImageView.setImage(new Image(new FileInputStream(destPath)));
                CustomAlertBox.showCustomAlert("Profile photo updated!", "success");
            }
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error updating photo: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleBack() {
        // Implement navigation back to doctor dashboard or previous view
        // This should be set by the parent controller
    }
} 