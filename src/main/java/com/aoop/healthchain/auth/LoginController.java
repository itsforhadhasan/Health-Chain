package com.aoop.healthchain.auth;

import com.aoop.healthchain.AdminDashboardController;
import com.aoop.healthchain.App;
import com.aoop.healthchain.DoctorDashboardController;
import com.aoop.healthchain.HospitalDashboardController;
import com.aoop.healthchain.PatientDashboardController;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static com.aoop.healthchain.util.Validation.isEmailValid;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMe;

    @FXML
    public void initialize() {
        String[] loginInfo = LoginInfoSave.getLoginInfo();
        if (loginInfo[0] != null && loginInfo[1] != null) {
            emailField.setText(loginInfo[0]);
            passwordField.setText(loginInfo[1]);
            rememberMe.setSelected(true);
        }
    }

    @FXML
    void login(ActionEvent event) throws SQLException, ClassNotFoundException {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            // Show an alert or error message
            CustomAlertBox.showCustomAlert("Please fill in all fields.", "error");
        }
        else if (!isEmailValid(email)) {
            // Show an alert or error message
            CustomAlertBox.showCustomAlert("Invalid email format.", "error");
        }
        else {
            DatabaseConnection connection = new DatabaseConnection();
            User userModel = new User(connection.getConnection());
            Optional<UserData> user = userModel.findByEmail(email);
            if(user.isPresent() && user.get().password().equals(Encryption.getSHA256(password))) {
              if(user.get().isVerified()){
                  // Save or clear login info based on "Remember Me" checkbox
                  if (rememberMe.isSelected()) {
                      LoginInfoSave.saveLoginInfo(email, password, user.get().role(), String.valueOf(user.get().id()));
                  } else {
                      LoginInfoSave.clearLoginInfo();
                  }

                  // Route to the appropriate dashboard based on user role
                  FXMLScene fxmlScene;
                  String role = user.get().role();

                  if (role != null) {
                      switch (role.toUpperCase()) {
                          case "ADMIN":
                              fxmlScene = FXMLScene.load("admin_dashboard.fxml");
                              AdminDashboardController adminController = (AdminDashboardController) fxmlScene.getController();
                              if (adminController != null) {
                                  adminController.setUser(user.get());
                              }
                              break;
                          case "HOSPITAL":
                              fxmlScene = FXMLScene.load("hospital_dashboard.fxml");
                              HospitalDashboardController hospitalController = (HospitalDashboardController) fxmlScene.getController();
                              if (hospitalController != null) {
                                  hospitalController.setUser(user.get());
                              }
                              break;
                          case "DOCTOR":
                              fxmlScene = FXMLScene.load("doctor_dashboard.fxml");
                              DoctorDashboardController doctorController = (DoctorDashboardController) fxmlScene.getController();
                              if (doctorController != null) {
                                  doctorController.setUser(user.get());
                              }
                              break;
                          case "PATIENT":
                          default:
                              fxmlScene = FXMLScene.load("dashboard.fxml");
                              PatientDashboardController patientController = (PatientDashboardController) fxmlScene.getController();
                              if (patientController != null) {
                                  patientController.setUser(user.get());
                              }
                              break;
                      }
                  } else {
                      // Default to patient dashboard if role is null
                      fxmlScene = FXMLScene.load("dashboard.fxml");
                      PatientDashboardController controller = (PatientDashboardController) fxmlScene.getController();
                      if (controller != null) {
                          controller.setUser(user.get());
                      }
                  }

                  FXMLScene.switchScene(fxmlScene, (Node)event.getSource());
                  CustomAlertBox.showCustomAlert("Login successful!", "success");
              } else {
                  String code = String.valueOf((int) (Math.random() * 1000000));
                  EmailTemplate emailTemplate = new EmailTemplate(user.get().fullName(), code);
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
            } else {
                // Show an alert or error message
                CustomAlertBox.showCustomAlert("User and password doesn't match.", "error");
            }

        }

    }

    @FXML
    void createAccount(ActionEvent event) throws IOException {
        try {
            FXMLScene.switchScene(FXMLScene.load("auth/register.fxml"), (Node) event.getSource());
        } catch (Exception e) {
            // Handle the error appropriately
            System.err.println("Error loading register.fxml: " + e.getMessage());
            throw e;
        }
    }

    @FXML
    void forgetPass(ActionEvent event) {
        try {
//            FXMLScene.switchScene(FXMLScene.load("auth/forget.fxml"), (Node) event.getSource());
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("auth/forget.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            // Handle the error appropriately
            System.err.println("Error loading register.fxml: " + e.getMessage());
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
