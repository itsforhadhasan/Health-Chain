package com.aoop.healthchain.auth;

import com.aoop.healthchain.PatientDashboardController;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.aoop.healthchain.util.Validation.isEmailValid;
import static com.aoop.healthchain.util.Validation.isValidPhoneNumber;

public class RegisterController implements Initializable {

    @FXML
    private ComboBox<String> bloodGroupField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField numberField;

    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        bloodGroupField.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    }



    @FXML
    void handleRegister(ActionEvent event) throws SQLException, ClassNotFoundException {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String number = numberField.getText();
        String bloodGroup = bloodGroupField.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty() || bloodGroup == null) {
            CustomAlertBox.showCustomAlert("Please fill in all fields.", "error");
        }
        else if (!isEmailValid(email)) {
            CustomAlertBox.showCustomAlert("Invalid email format.", "error");
        } else if (!isValidPhoneNumber(number)) {
            CustomAlertBox.showCustomAlert("Invalid phone number format.", "error");
        } else if (password.length() < 8) {
            CustomAlertBox.showCustomAlert("Password must be at least 8 characters long.", "error");
        }
        else {
            if (number.startsWith("+8801")) {
                number = number.replace("+8801", "01");
            }

            DatabaseConnection db = new DatabaseConnection();
            try (Connection connection = db.getConnection()) {
                User userModel = new User(connection);

                // Find user by email
                Optional<UserData> user = userModel.findByEmail(email);
                if(user.isPresent()){
                    CustomAlertBox.showCustomAlert("User already exists!", "error");
                }
                else {
                    // Create a new user
                    Long userId = userModel.create(
                            email,
                            password,
                            name,
                            number,
                            bloodGroup,
                            "PATIENT"
                    );

                    String code = String.valueOf((int) (Math.random() * 1000000));
//                    String htmlContent = "<h1>Verification Code</h1><p>Your verification code is: <strong>" + code + "</strong></p>";

                    EmailTemplate emailTemplate = new EmailTemplate(name, code);
                    // Show loading indicator
                    Stage loadingStage = createLoadingStage();
                    loadingStage.show();

                    // Send email in background thread
                    new Thread(() -> {
                        try {
                            SendEmail.sendMail(email, "Verification Code", emailTemplate.getRegisterTemplate());
                            
                            // Update UI in JavaFX thread
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    loadingStage.close();
                                    FXMLScene fxmlScene = FXMLScene.load("auth/registerVerification.fxml");
                                    RegisterVerificationController controller = (RegisterVerificationController) fxmlScene.getController();
                                    controller.setCode(code);
                                    controller.setEmail(email);
                                    FXMLScene.switchScene(fxmlScene, (Node) event.getSource());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    CustomAlertBox.showCustomAlert("Error loading verification page: " + e.getMessage(), "error");
                                }
                            });
                        } catch (Exception e) {
                            javafx.application.Platform.runLater(() -> {
                                loadingStage.close();
                                CustomAlertBox.showCustomAlert("Error sending email: " + e.getMessage(), "error");
                            });
                        }
                    }).start();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("Database error: " + e.getMessage(), "error");
            } catch (Exception e) {
                e.printStackTrace();
                CustomAlertBox.showCustomAlert("An unexpected error occurred: " + e.getMessage(), "error");
            }
        }
    }

    private Stage createLoadingStage() {
        Stage loadingStage = new Stage(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        
        Label loadingLabel = new Label("Sending verification email...");
        javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingBox.setStyle("-fx-background-color: white; -fx-padding: 20px;");
        
        Scene scene = new Scene(loadingBox);
        loadingStage.setScene(scene);
        loadingStage.setWidth(200);
        loadingStage.setHeight(100);
        loadingStage.centerOnScreen();
        
        return loadingStage;
    }
}