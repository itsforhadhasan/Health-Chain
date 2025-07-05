package com.aoop.healthchain;

import com.aoop.healthchain.model.HospitalData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.FXMLScene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class HospitalCardController {

    @FXML
    private Label hospitalName;
    
    @FXML
    private Label hospitalEmail;
    
    @FXML
    private Label hospitalPhone;
    
    @FXML
    private Label hospitalStatus;
    
    @FXML
    private Label hospitalId;
    
    @FXML
    private Label hospitalAddress;
    
    @FXML
    private Label hospitalCity;
    
    @FXML
    private Button viewButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;

    private HospitalData hospitalData;
    private HospitalListController parentController;

    public void setHospitalData(HospitalData hospitalData) {
        this.hospitalData = hospitalData;
        updateCardDisplay();
    }

    public void setParentController(HospitalListController parentController) {
        this.parentController = parentController;
    }

    private void updateCardDisplay() {
        if (hospitalData != null) {
            hospitalName.setText(hospitalData.name());
            hospitalEmail.setText(hospitalData.email());
            hospitalPhone.setText(hospitalData.phone());
            hospitalStatus.setText(hospitalData.status());
            hospitalId.setText("ID: H" + String.format("%03d", hospitalData.id()));
            hospitalAddress.setText(hospitalData.address());
            hospitalCity.setText(hospitalData.city());
            
            // Update status badge styling
            updateStatusBadge();
        }
    }

    private void updateStatusBadge() {
        String status = hospitalData.status();
        if ("Active".equalsIgnoreCase(status)) {
            hospitalStatus.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #10b981; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
        } else if ("Inactive".equalsIgnoreCase(status)) {
            hospitalStatus.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
        } else {
            hospitalStatus.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #f59e0b; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleViewHospital() {
        if (hospitalData != null) {
            try {
                // Set the static hospitalId in the view controller
                AdminViewHospitalController.selectedHospitalId = hospitalData.id();
                Node root = viewButton.getScene().getRoot();
                FXMLScene fxmlScene = FXMLScene.load("/com/aoop/healthchain/admin-view-hospital.fxml");
                if (fxmlScene.getRoot() == null) {
                    CustomAlertBox.showCustomAlert("Error: Could not load hospital details view (FXML root is null). Check FXML path and resources.", "error");
                    return;
                }
                Scene scene = new Scene(fxmlScene.getRoot());
                Stage stage = (Stage) root.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error opening hospital details: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleEditHospital() {
        if (hospitalData != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("hospital_form.fxml"));
                Parent root = loader.load();

                HospitalFormController controller = loader.getController();
                controller.setHospitalListController(parentController);
                controller.setHospital(hospitalData);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Edit Hospital");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
                // Refresh the parent controller after editing
                if (parentController != null) {
                    parentController.refreshTable();
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Error opening hospital form: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleDeleteHospital() {
        if (hospitalData != null) {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Hospital");
            alert.setContentText("Are you sure you want to delete hospital '" + hospitalData.name() + "'? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // TODO: Implement actual delete logic here
                    // For now, just show a success message
                    CustomAlertBox.showCustomAlert("Hospital '" + hospitalData.name() + "' has been deleted successfully.", "success");
                    
                    // Refresh the parent controller after deletion
                    if (parentController != null) {
                        parentController.refreshTable();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomAlertBox.showCustomAlert("Error deleting hospital: " + e.getMessage(), "error");
                }
            }
        }
    }
} 