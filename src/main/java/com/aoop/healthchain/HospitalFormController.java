package com.aoop.healthchain;

import com.aoop.healthchain.model.Hospital;
import com.aoop.healthchain.model.HospitalData;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.EmailTemplate;
import com.aoop.healthchain.util.SendEmail;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.time.Year;
import java.util.ResourceBundle;

public class HospitalFormController implements Initializable {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private TextField websiteField;
    @FXML private TextField licenseField;
    @FXML private TextField establishedYearField;
    @FXML private TextField bedCountField;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private HospitalListController hospitalListController;
    private HospitalData existingHospital;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize status combobox
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE", "PENDING");
        statusComboBox.setValue("ACTIVE");

        // Add input validation
        setupValidation();
    }

    public void setHospitalListController(HospitalListController controller) {
        this.hospitalListController = controller;
    }

    public void setHospital(HospitalData hospital) {
        this.existingHospital = hospital;
        this.isEditMode = true;

        // Populate fields with existing data
        nameField.setText(hospital.name());
        emailField.setText(hospital.email());
        phoneField.setText(hospital.phone());
        addressField.setText(hospital.address());
        cityField.setText(hospital.city());
        stateField.setText(hospital.state());
        zipField.setText(hospital.zip());
        websiteField.setText(hospital.website());
        licenseField.setText(hospital.licenseNumber());
        establishedYearField.setText(String.valueOf(hospital.establishmentYear()));
        bedCountField.setText(String.valueOf(hospital.bedCount()));
        statusComboBox.setValue(hospital.status());

        // Update button text
        saveButton.setText("Update Hospital");
    }

    private void setupValidation() {
        // Add real-time validation for required fields
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(nameField, !newVal.trim().isEmpty(), "Hospital name is required");
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(emailField, newVal.matches("^[\\w\\-\\.\\+]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$"), "Valid email is required");
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(phoneField, !newVal.trim().isEmpty(), "Phone number is required");
        });

        establishedYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int year = Integer.parseInt(newVal);
                    int currentYear = Year.now().getValue();
                    validateField(establishedYearField, year > 1800 && year <= currentYear, "Year must be between 1800 and " + currentYear);
                } catch (NumberFormatException e) {
                    validateField(establishedYearField, false, "Year must be a number");
                }
            }
        });

        bedCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int beds = Integer.parseInt(newVal);
                    validateField(bedCountField, beds > 0, "Bed count must be positive");
                } catch (NumberFormatException e) {
                    validateField(bedCountField, false, "Bed count must be a number");
                }
            }
        });
    }

    private void validateField(TextField field, boolean isValid, String errorMessage) {
        if (!isValid) {
            field.setStyle("-fx-border-color: red;");
            field.setTooltip(new Tooltip(errorMessage));
        } else {
            field.setStyle("");
            field.setTooltip(null);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            CustomAlertBox.showCustomAlert("Please fill all required fields correctly.", "error");
            return;
        }

        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            Connection connection = dbConnection.getConnection();

            // Start transaction
            connection.setAutoCommit(false);

            try {
                if (isEditMode) {
                    // Update existing hospital
                    updateExistingHospital(connection);
                } else {
                    // Create new hospital with user account
                    createNewHospital(connection);
                }

                // Commit transaction
                connection.commit();

                // Refresh hospital list and close form
                if (hospitalListController != null) {
                    hospitalListController.refreshTable();
                }
                closeForm();

            } catch (Exception e) {
                // Rollback transaction on error
                connection.rollback();
                throw e;
            } finally {
                // Restore auto-commit
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error saving hospital: " + e.getMessage(), "error");
        }
    }

    private void updateExistingHospital(Connection connection) throws Exception {
        Hospital hospitalModel = new Hospital(connection);

        HospitalData updatedHospital = new HospitalData(
            existingHospital.id(),
            nameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            addressField.getText(),
            cityField.getText(),
            stateField.getText(),
            zipField.getText(),
            websiteField.getText(),
            licenseField.getText(),
            Integer.parseInt(establishedYearField.getText()),
            Integer.parseInt(bedCountField.getText()),
            statusComboBox.getValue(),
            existingHospital.userId(),
            existingHospital.userEmail(),
            existingHospital.userRole(),
            existingHospital.createdAt(),
            existingHospital.updatedAt()
        );

        boolean success = hospitalModel.update(updatedHospital);
        if (success) {
            CustomAlertBox.showCustomAlert("Hospital updated successfully!", "success");
        } else {
            throw new Exception("Failed to update hospital");
        }
    }

    private void createNewHospital(Connection connection) throws Exception {
        // 1. Create user account for hospital
        User userModel = new User(connection);

        // Generate a random password for the hospital
        String password = generateRandomPassword();

        // Create a new user with HOSPITAL role
        Long userId = userModel.create(
            emailField.getText(),
            password,
                nameField.getText(),


                phoneField.getText(),
            "",  // bloodGroup (not needed for hospitals)
            "HOSPITAL"
        );

        if (userId <= 0) {
            throw new Exception("Failed to create user account");
        }

        // 2. Create hospital record
        Hospital hospitalModel = new Hospital(connection);

        HospitalData newHospital = HospitalData.createNew(
            nameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            addressField.getText(),
            cityField.getText(),
            stateField.getText(),
            zipField.getText(),
            websiteField.getText(),
            licenseField.getText(),
            Integer.parseInt(establishedYearField.getText()),
            Integer.parseInt(bedCountField.getText())
        );

        int hospitalId = hospitalModel.create(newHospital, Math.toIntExact(userId));
        if (hospitalId <= 0) {
            throw new Exception("Failed to create hospital record");
        }

        // 3. Send email with credentials
        sendCredentialEmail(emailField.getText(), nameField.getText(), password);

        CustomAlertBox.showCustomAlert("Hospital created successfully and credentials sent!", "success");
    }

    private String generateRandomPassword() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String SPECIAL = "!@#$%^&*()_-+=<>?";

        String DATA_FOR_PASSWORD = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(DATA_FOR_PASSWORD.length());
            sb.append(DATA_FOR_PASSWORD.charAt(randomIndex));
        }

        return sb.toString();
    }

    private void sendCredentialEmail(String email, String hospitalName, String password) {
        // Show loading indicator
        Stage loadingStage = createLoadingStage();
        loadingStage.show();

        // Send email in background thread
        new Thread(() -> {
            try {
                EmailTemplate emailTemplate = new EmailTemplate(hospitalName, "");
                String subject = "Welcome to HealthChain - Your Hospital Account";
                String content = createHospitalWelcomeEmail(hospitalName, email, password);

                SendEmail.sendMail(email, subject, content);

                // Update UI in JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    CustomAlertBox.showCustomAlert("Error sending credentials email: " + e.getMessage(), "error");
                });
            }
        }).start();
    }

    private String createHospitalWelcomeEmail(String hospitalName, String email, String password) {
        return "<html><body>" +
               "<h1>Welcome to HealthChain</h1>" +
               "<p>Dear " + hospitalName + ",</p>" +
               "<p>Your hospital account has been created successfully. Below are your login credentials:</p>" +
               "<p><strong>Email:</strong> " + email + "</p>" +
               "<p><strong>Password:</strong> " + password + "</p>" +
               "<p>Please login to the HealthChain platform to update your hospital information and manage your services.</p>" +
               "<p>For security reasons, we recommend changing your password after the first login.</p>" +
               "<p>Best regards,<br>The HealthChain Team</p>" +
               "</body></html>";
    }

    private Stage createLoadingStage() {
        Stage loadingStage = new Stage(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);

        Label loadingLabel = new Label("Sending credentials email...");
        javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();

        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingBox.setStyle("-fx-background-color: white; -fx-padding: 20px;");

        javafx.scene.Scene scene = new javafx.scene.Scene(loadingBox);
        loadingStage.setScene(scene);
        loadingStage.setWidth(200);
        loadingStage.setHeight(100);
        loadingStage.centerOnScreen();

        return loadingStage;
    }

    private boolean validateForm() {
        // Check required fields
        if (nameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty() ||
            cityField.getText().trim().isEmpty() ||
            stateField.getText().trim().isEmpty() ||
            zipField.getText().trim().isEmpty() ||
            licenseField.getText().trim().isEmpty() ||
            establishedYearField.getText().trim().isEmpty() ||
            bedCountField.getText().trim().isEmpty()) {
            return false;
        }

        // Validate email format
        if (!emailField.getText().matches("^[\\w\\-\\.\\+]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$")) {
            return false;
        }

        // Validate numeric fields
        try {
            int year = Integer.parseInt(establishedYearField.getText());
            int currentYear = Year.now().getValue();
            if (year < 1800 || year > currentYear) {
                return false;
            }

            int beds = Integer.parseInt(bedCountField.getText());
            if (beds <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
