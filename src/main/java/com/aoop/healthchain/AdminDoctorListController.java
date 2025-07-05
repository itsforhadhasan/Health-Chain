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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AdminDoctorListController implements Initializable {
    @FXML private TextField searchField;
    @FXML private FlowPane doctorsCardPane;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;

    private ObservableList<DoctorData> doctorsList = FXCollections.observableArrayList();
    private FilteredList<DoctorData> filteredDoctors;
    private UserData userData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSearchField();
        loadDoctors();
    }

    public void setUser(UserData userData) {
        this.userData = userData;
        if (userData != null) {
            userNameLabel.setText(userData.fullName());
            userEmailLabel.setText(userData.email());
        }
    }

    private void setupSearchField() {
        filteredDoctors = new FilteredList<>(doctorsList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDoctors.setPredicate(createPredicate(newValue));
            loadDoctorCards(); // Refresh cards when search changes
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
            } else if (doctor.specialization().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (doctor.status().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }

            return false;
        };
    }

    private void loadDoctors() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            Doctor doctorModel = new Doctor(connection.getConnection());
            List<DoctorData> doctors = doctorModel.findAll();

            doctorsList.clear();
            doctorsList.addAll(doctors);
            loadDoctorCards();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading doctors: " + e.getMessage(), "error");
        }
    }

    private void loadDoctorCards() {
        doctorsCardPane.getChildren().clear();
        
        for (DoctorData doctor : filteredDoctors) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-doctorcard.fxml"));
                Parent cardRoot = loader.load();
                
                AdminDoctorCardController cardController = loader.getController();
                cardController.setDoctorData(doctor);
                cardController.setParentController(this);
                
                doctorsCardPane.getChildren().add(cardRoot);
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error loading doctor card: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleCreateDoctor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_form.fxml"));
            Parent root = loader.load();

            DoctorFormController controller = loader.getController();
            // Create a temporary DoctorListController to pass to the form
            DoctorListController tempController = new DoctorListController();
            controller.setDoctorListController(tempController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the admin doctor list after form closes
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening doctor form: " + e.getMessage(), "error");
        }
    }

    public void editDoctor(DoctorData doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor_form.fxml"));
            Parent root = loader.load();

            DoctorFormController controller = loader.getController();
            // Create a temporary DoctorListController to pass to the form
            DoctorListController tempController = new DoctorListController();
            controller.setDoctorListController(tempController);
            controller.setDoctor(doctor);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the admin doctor list after form closes
            refreshTable();
        } catch (Exception e) {
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
                // TODO: Implement actual delete logic here
                // For now, just show a success message
                CustomAlertBox.showCustomAlert("Doctor '" + doctor.name() + "' has been deleted successfully.", "success");
                refreshTable();
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
            FXMLScene.switchScene(FXMLScene.load("admin_dashboard.fxml"), doctorsCardPane.getScene().getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error returning to dashboard: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleUsers() {
        // TODO: Implement scene switch to user management
        System.out.println("Users section selected");
    }

    @FXML
    private void handleHospitals() {
        try {
            FXMLScene.switchScene(FXMLScene.load("hospital_list.fxml"), doctorsCardPane.getScene().getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading hospital list: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleDoctors() {
        // Already on this page, do nothing or refresh
        System.out.println("Doctors section selected");
        refreshTable();
    }

    @FXML
    private void handleReports() {
        // TODO: Implement scene switch to reports
        System.out.println("Reports section selected");
    }

    @FXML
    private void handleSettings() {
        // TODO: Implement scene switch to settings
        System.out.println("Settings section selected");
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), doctorsCardPane.getScene().getRoot());
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error logging out: " + e.getMessage(), "error");
        }
    }
} 