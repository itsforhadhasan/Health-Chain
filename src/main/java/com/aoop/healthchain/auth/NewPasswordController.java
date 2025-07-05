package com.aoop.healthchain.auth;

import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class NewPasswordController {
    private String email;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private PasswordField newPassword;

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    void setNewPassword(ActionEvent event) throws SQLException, ClassNotFoundException {

        String newPasswordText = newPassword.getText();
        String confirmPasswordText = confirmPassword.getText();

        if (newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
            // Show an alert or error message
            CustomAlertBox.showCustomAlert("Please fill in all fields.", "error");
        } else if (!newPasswordText.equals(confirmPasswordText)) {
            // Show an alert or error message
            CustomAlertBox.showCustomAlert("Passwords do not match.", "error");
        }
        else if (newPasswordText.length() < 8) {
            CustomAlertBox.showCustomAlert("Password must be at least 8 characters long.", "error");
        }
        else {
            DatabaseConnection connection = new DatabaseConnection();
            User userModel = new User(connection.getConnection());
            Optional<UserData> user = userModel.findByEmail(email);
            if (user.isPresent()) {
                userModel.updatePassword(user.get().id(), newPasswordText);
                userModel.updateVerificationStatus(user.get().id(), true);
                //close stage
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();

                CustomAlertBox.showCustomAlert("Password updated successfully.", "success");

            } else {
                CustomAlertBox.showCustomAlert("User not found.", "error");
            }
        }

    }

}
