package com.aoop.healthchain;

import com.aoop.healthchain.model.Hospital;
import com.aoop.healthchain.model.HospitalData;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class HospitalListController implements Initializable {
    @FXML private TextField searchField;
    @FXML private FlowPane hospitalsCardPane;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;

    private ObservableList<HospitalData> hospitalsList = FXCollections.observableArrayList();
    private FilteredList<HospitalData> filteredHospitals;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSearchField();
        loadHospitals();
    }

    private void setupSearchField() {
        filteredHospitals = new FilteredList<>(hospitalsList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredHospitals.setPredicate(createPredicate(newValue));
            loadHospitalCards(); // Refresh cards when search changes
        });
    }

    private Predicate<HospitalData> createPredicate(String searchText) {
        return hospital -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            if (hospital.name().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (hospital.email().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (hospital.city().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (hospital.status().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }

            return false;
        };
    }

    private void loadHospitals() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            Hospital hospitalModel = new Hospital(connection.getConnection());
            List<HospitalData> hospitals = hospitalModel.findAll();

            hospitalsList.clear();
            hospitalsList.addAll(hospitals);
            loadHospitalCards();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading hospitals: " + e.getMessage(), "error");
        }
    }

    private void loadHospitalCards() {
        hospitalsCardPane.getChildren().clear();
        
        for (HospitalData hospital : filteredHospitals) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("hospital-card.fxml"));
                Parent cardRoot = loader.load();
                
                HospitalCardController cardController = loader.getController();
                cardController.setHospitalData(hospital);
                cardController.setParentController(this);
                
                hospitalsCardPane.getChildren().add(cardRoot);
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error loading hospital card: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleCreateHospital() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hospital_form.fxml"));
            Parent root = loader.load();

            HospitalFormController controller = loader.getController();
            controller.setHospitalListController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create Hospital");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening hospital form: " + e.getMessage(), "error");
        }
    }

    private void editHospital(HospitalData hospital) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hospital_form.fxml"));
            Parent root = loader.load();

            HospitalFormController controller = loader.getController();
            controller.setHospitalListController(this);
            controller.setHospital(hospital);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Hospital");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error opening hospital form: " + e.getMessage(), "error");
        }
    }

    private void deleteHospital(HospitalData hospital) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Hospital");
        alert.setContentText("Are you sure you want to delete hospital '" + hospital.name() + "'? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // TODO: Implement actual delete logic here
                // For now, just show a success message
                CustomAlertBox.showCustomAlert("Hospital '" + hospital.name() + "' has been deleted successfully.", "success");
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error deleting hospital: " + e.getMessage(), "error");
            }
        }
    }

    public void refreshTable() {
        loadHospitals();
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLScene.switchScene(FXMLScene.load("admin_dashboard.fxml"), hospitalsCardPane.getScene().getRoot());
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
        // Already on this page, do nothing or refresh
        System.out.println("Hospitals section selected");
        refreshTable();
    }

    @FXML
    private void handleDoctors() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("admin-doctorlist.fxml");
            AdminDoctorListController controller = (AdminDoctorListController) fxmlScene.getController();
            if (controller != null) {
                // Since this is from admin context, we'll pass null and let the controller handle it
                controller.setUser(null);
                FXMLScene.switchScene(fxmlScene, hospitalsCardPane.getScene().getRoot());
            } else {
                CustomAlertBox.showCustomAlert("Error: Could not load admin doctor list controller", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading admin doctor list: " + e.getMessage(), "error");
        }
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
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), hospitalsCardPane.getScene().getRoot());
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error logging out: " + e.getMessage(), "error");
        }
    }
}
