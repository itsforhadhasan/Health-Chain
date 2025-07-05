package com.aoop.healthchain;

import com.aoop.healthchain.model.Doctor;
import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class DoctorListController implements Initializable {
    @FXML private TextField searchField;
    @FXML private VBox doctorsContainer;
    @FXML private Label hospitalNameLabel;
    @FXML private Label hospitalEmailLabel;

    private ObservableList<DoctorData> doctorsList = FXCollections.observableArrayList();
    private FilteredList<DoctorData> filteredDoctors;
    private UserData currentUser;
    private int hospitalId = -1; // Store hospital ID for filtering and creating doctors

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSearchField();
        loadDoctors();
    }

    public void setUser(UserData user) {
        this.currentUser = user;
        updateUserInfo();
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
        loadDoctors(); // Reload doctors with hospital filter
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            hospitalNameLabel.setText(currentUser.fullName());
            hospitalEmailLabel.setText(currentUser.email());
        } else {
            // Set default values if no user data is available
            hospitalNameLabel.setText("Hospital");
            hospitalEmailLabel.setText("hospital@healthchain.com");
        }
    }

    private void setupSearchField() {
        filteredDoctors = new FilteredList<>(doctorsList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDoctors.setPredicate(createPredicate(newValue));
            loadDoctorRows(); // Refresh rows when search changes
        });
    }

    private Predicate<DoctorData> createPredicate(String searchText) {
        return doctor -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            if (doctor.name().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (doctor.email().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (doctor.specialization() != null && doctor.specialization().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (doctor.status() != null && doctor.status().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }

            return false;
        };
    }

    private void loadDoctors() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            Doctor doctorModel = new Doctor(connection.getConnection());
            List<DoctorData> doctors;
            
            if (hospitalId > 0) {
                // Filter doctors by hospital ID
                doctors = doctorModel.findByHospitalId(hospitalId);
            } else {
                // Load all doctors if no hospital ID is set
                doctors = doctorModel.findAll();
            }

            doctorsList.clear();
            doctorsList.addAll(doctors);
            loadDoctorRows(); // Load the custom rows
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading doctors: " + e.getMessage(), "error");
        }
    }

    private void loadDoctorRows() {
        doctorsContainer.getChildren().clear();
        
        for (DoctorData doctor : filteredDoctors) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_row.fxml"));
                Parent rowRoot = loader.load();
                
                DoctorRowController rowController = loader.getController();
                rowController.setDoctorData(doctor);
                rowController.setParentController(this);
                
                doctorsContainer.getChildren().add(rowRoot);
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error loading doctor row: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleCreateDoctor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_form.fxml"));
            Parent root = loader.load();

            DoctorFormController controller = loader.getController();
            controller.setDoctorListController(this);
            controller.setHospitalId(hospitalId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening doctor form: " + e.getMessage(), "error");
        }
    }

    public void viewDoctorDetails(DoctorData doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_view.fxml"));
            Parent root = loader.load();

            DoctorViewController controller = loader.getController();
            controller.setDoctorData(doctor);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Doctor Details - " + doctor.name());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening doctor details: " + e.getMessage(), "error");
        }
    }

    public void editDoctor(DoctorData doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_form.fxml"));
            Parent root = loader.load();

            DoctorFormController controller = loader.getController();
            controller.setDoctorListController(this);
            controller.setHospitalId(hospitalId);
            controller.setDoctor(doctor);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Doctor - " + doctor.name());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening doctor form: " + e.getMessage(), "error");
        }
    }

    public void deleteDoctor(DoctorData doctor) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Doctor");
        alert.setContentText("Are you sure you want to delete doctor '" + doctor.name() + "'? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DatabaseConnection connection = new DatabaseConnection();
                Doctor doctorModel = new Doctor(connection.getConnection());
                
                boolean success = doctorModel.delete(doctor.id());
                
                if (success) {
                    CustomAlertBox.showCustomAlert("Doctor '" + doctor.name() + "' has been deleted successfully.", "success");
                    refreshTable();
                } else {
                    CustomAlertBox.showCustomAlert("Failed to delete doctor. Please try again.", "error");
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error deleting doctor: " + e.getMessage(), "error");
            }
        }
    }

    public void refreshTable() {
        loadDoctors();
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("hospital_dashboard.fxml");
            HospitalDashboardController controller = (HospitalDashboardController) fxmlScene.getController();
            
            // Only set user if we have user data, otherwise let the hospital dashboard handle it
            if (currentUser != null) {
                controller.setUser(currentUser);
            }
            
            FXMLScene.switchScene(fxmlScene, doctorsContainer.getScene().getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error returning to dashboard: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleDoctors() {
        try {
            // Already on doctors page, do nothing or refresh
            loadDoctors();
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error refreshing doctors: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handlePatients() {
        try {
            // TODO: Navigate to patients page
            CustomAlertBox.showCustomAlert("Patients page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to patients page: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleAppointments() {
        try {
            // TODO: Navigate to appointments page
            CustomAlertBox.showCustomAlert("Appointments page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to appointments page: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleDepartments() {
        try {
            // TODO: Navigate to departments page
            CustomAlertBox.showCustomAlert("Departments page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to departments page: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleReports() {
        try {
            // TODO: Navigate to reports page
            CustomAlertBox.showCustomAlert("Reports page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to reports page: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleSettings() {
        try {
            // TODO: Navigate to settings page
            CustomAlertBox.showCustomAlert("Settings page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to settings page: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), doctorsContainer.getScene().getRoot());
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error logging out: " + e.getMessage(), "error");
        }
    }
} 