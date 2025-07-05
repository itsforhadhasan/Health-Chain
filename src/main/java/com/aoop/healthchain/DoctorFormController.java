package com.aoop.healthchain;

import com.aoop.healthchain.model.Doctor;
import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.model.Role;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class DoctorFormController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField specializationField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private TextField licenseField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;

    private DoctorListController doctorListController;
    private DoctorData doctorToEdit;
    private int hospitalId = -1; // Store hospital ID for creating doctors

    public void setDoctorListController(DoctorListController doctorListController) {
        this.doctorListController = doctorListController;
    }

    public void setDoctor(DoctorData doctor) {
        this.doctorToEdit = doctor;
        if (doctor != null) {
            populateForm();
        }
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    private void populateForm() {
        nameField.setText(doctorToEdit.name());
        emailField.setText(doctorToEdit.email());
        phoneField.setText(doctorToEdit.phone());
        specializationField.setText(doctorToEdit.specialization());
        addressField.setText(doctorToEdit.address());
        cityField.setText(doctorToEdit.city());
        stateField.setText(doctorToEdit.state());
        zipField.setText(doctorToEdit.zip());
        licenseField.setText(doctorToEdit.licenseNumber());
        issueDatePicker.setValue(LocalDate.parse(doctorToEdit.issueDate()));
        expiryDatePicker.setValue(LocalDate.parse(doctorToEdit.expiryDate()));
        passwordField.setDisable(true); // Don't allow password change on edit for now
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();
        String specialization = specializationField.getText();
        String address = addressField.getText();
        String city = cityField.getText();
        String state = stateField.getText();
        String zip = zipField.getText();
        String license = licenseField.getText();
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || specialization.isEmpty() || address.isEmpty() || city.isEmpty() || state.isEmpty() || zip.isEmpty() || license.isEmpty() || issueDate == null || expiryDate == null) {
            CustomAlertBox.showCustomAlert("Please fill in all fields.", "error");
            return;
        }

        if (doctorToEdit == null && password.isEmpty()) {
            CustomAlertBox.showCustomAlert("Password is required for new doctors.", "error");
            return;
        }

        try {
            DatabaseConnection db = new DatabaseConnection();
            User userModel = new User(db.getConnection());
            Doctor doctorModel = new Doctor(db.getConnection());

            if (doctorToEdit == null) { // Create new doctor
                // 1. Create a new user with the DOCTOR role
                int userId = userModel.create(name, email, phone, password, Role.DOCTOR);

                if (userId == -1) {
                    CustomAlertBox.showCustomAlert("Failed to create a new user for the doctor.", "error");
                    return;
                }

                // 2. Create the doctor profile
                boolean success = doctorModel.create(userId, specialization, phone, address, city, state, zip, license, issueDate.toString(), expiryDate.toString(), hospitalId);

                if (success) {
                    CustomAlertBox.showCustomAlert("Doctor created successfully.", "success");
                    closeWindow();
                } else {
                    CustomAlertBox.showCustomAlert("Failed to create doctor.", "error");
                }
            } else { // Update existing doctor
                // Update the doctor profile
                boolean success = doctorModel.update(doctorToEdit.id(), specialization, phone, address, city, state, zip, license, issueDate.toString(), expiryDate.toString(), doctorToEdit.status());

                if (success) {
                    CustomAlertBox.showCustomAlert("Doctor updated successfully.", "success");
                    closeWindow();
                } else {
                    CustomAlertBox.showCustomAlert("Failed to update doctor.", "error");
                }
            }

            doctorListController.refreshTable();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Database error: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 