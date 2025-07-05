package com.aoop.healthchain;

import com.aoop.healthchain.model.DoctorData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDoctorCardController implements Initializable {
    @FXML private ImageView doctorImageView;
    @FXML private Label doctorNameLabel;
    @FXML private Label specializationLabel;
    @FXML private Label hospitalLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label licenseLabel;
    @FXML private Label statusBadge;

    private DoctorData doctorData;
    private AdminDoctorListController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize any card-specific setup here
    }

    public void setDoctorData(DoctorData doctorData) {
        this.doctorData = doctorData;
        updateCardDisplay();
    }

    public void setParentController(AdminDoctorListController parentController) {
        this.parentController = parentController;
    }

    private void updateCardDisplay() {
        if (doctorData != null) {
            doctorNameLabel.setText("Dr. " + doctorData.name());
            specializationLabel.setText(doctorData.specialization());
            hospitalLabel.setText(doctorData.hospitalName() != null ? doctorData.hospitalName() : "No Hospital Assigned");
            emailLabel.setText(doctorData.email());
            phoneLabel.setText(doctorData.phone());
            addressLabel.setText(doctorData.address() + ", " + doctorData.city());
            licenseLabel.setText("License: " + doctorData.licenseNumber());
            statusBadge.setText(doctorData.status());

            // Set status badge color
            if ("ACTIVE".equals(doctorData.status())) {
                statusBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;");
            } else if ("INACTIVE".equals(doctorData.status())) {
                statusBadge.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;");
            } else {
                statusBadge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;");
            }

            // Use default male image since gender is not available in DoctorData
            String imagePath = "images/default_male.png";
            
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                doctorImageView.setImage(image);
            } catch (Exception e) {
                // Use default image if loading fails
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("images/default_male.png"));
                    doctorImageView.setImage(defaultImage);
                } catch (Exception ex) {
                    // If even default image fails, leave as is
                }
            }
        }
    }

    @FXML
    private void handleView() {
        if (doctorData != null) {
            // TODO: Implement view doctor details functionality
            System.out.println("View doctor: " + doctorData.name());
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