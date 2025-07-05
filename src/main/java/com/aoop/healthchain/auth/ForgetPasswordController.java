package com.aoop.healthchain.auth;

import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

import static com.aoop.healthchain.util.Validation.isEmailValid;

public class ForgetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    void sendCode(ActionEvent event) throws Exception {
        String email = emailField.getText();
        if (!isEmailValid(email)) {
            CustomAlertBox.showCustomAlert("Invalid Email Address", "error");
            return;
        }

        DatabaseConnection connection = new DatabaseConnection();
        User userModel = new User(connection.getConnection());
        Optional<UserData> user = userModel.findByEmail(email);
        
        if (!user.isPresent()) {
            CustomAlertBox.showCustomAlert("No User Registered with this email", "error");
            return;
        }

        String code = String.valueOf((int) (Math.random() * 1000000));
        System.out.println(code);
        String htmlContent = "<h1>Verification Code</h1><p>Your verification code is: <strong>" + code + "</strong></p>";

        // Create loading indicator
        Stage loadingStage = new Stage(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);

        Label loadingLabel = new Label("Sending verification code...");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingBox.setStyle("-fx-background-color: white; -fx-padding: 20px;");

        Scene scene = new Scene(loadingBox);
        loadingStage.setScene(scene);
        loadingStage.setWidth(200);
        loadingStage.setHeight(100);
        loadingStage.centerOnScreen();
        loadingStage.show();

        EmailTemplate emailTemplate = new EmailTemplate(user.get().fullName(), code);
        // Send email in background thread
        new Thread(() -> {
            try {
                SendEmail.sendMail(email, "Verification Code", emailTemplate.getForgetPassTemplate());
                
                Platform.runLater(() -> {
                    try {
                        loadingStage.close();
                        FXMLScene fxmlScene = FXMLScene.load("auth/verificationCode.fxml");
                        VerificationCodeController controller = (VerificationCodeController) fxmlScene.getController();
                        controller.setCode(code);
                        controller.setEmail(email);
                        FXMLScene.switchScene(fxmlScene, (Node) event.getSource());
                    } catch (Exception e) {
                        CustomAlertBox.showCustomAlert("Error loading verification page: " + e.getMessage(), "error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingStage.close();
                    CustomAlertBox.showCustomAlert("Failed to send verification code: " + e.getMessage(), "error");
                });
            }
        }).start();
    }

}