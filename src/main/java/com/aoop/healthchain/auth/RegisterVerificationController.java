package com.aoop.healthchain.auth;

import com.aoop.healthchain.PatientDashboardController;
import com.aoop.healthchain.model.User;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.sql.SQLException;
import java.util.Optional;

public class RegisterVerificationController {

    private String code;
    private String email;

    @FXML private TextField code1;
    @FXML private TextField code2;
    @FXML private TextField code3;
    @FXML private TextField code4;
    @FXML private TextField code5;
    @FXML private TextField code6;

    private TextField[] fields;

    @FXML
    public void initialize() {
        fields = new TextField[]{code1, code2, code3, code4, code5, code6};

        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            TextField tf = fields[i];

            tf.setOnKeyReleased(event -> handleCode(event, index));
        }
    }
    @FXML
    public void handleCode(KeyEvent event, int index) {
        TextField currentField = fields[index];
        String text = currentField.getText();

        // Only allow one digit
        if (text.length() > 1) {
            currentField.setText(text.substring(0, 1));
        }

        // Move to next if valid digit entered
        if (!text.isEmpty() && text.matches("\\d")) {
            if (index < fields.length - 1) {
                fields[index + 1].requestFocus();
            }
        }

        // Handle backspace
        if (event.getCode() == KeyCode.BACK_SPACE && text.isEmpty()) {
            if (index > 0) {
                fields[index - 1].requestFocus();
            }
        }
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    private boolean isValidCode() {
        StringBuilder sb = new StringBuilder();
        for (TextField tf : fields) {
            sb.append(tf.getText());
        }
        return code != null && code.contentEquals(sb);
    }

    @FXML
    void confirmVerification(ActionEvent event) throws SQLException, ClassNotFoundException {
        if (isValidCode()) {
            DatabaseConnection connection = new DatabaseConnection();
            User userModel = new User(connection.getConnection());
            Optional<UserData> user = userModel.findByEmail(email);
            if(user.isPresent()){
                userModel.updateVerificationStatus(user.get().id(), true);
                FXMLScene fxmlScene =  FXMLScene.load("dashboard.fxml");
                PatientDashboardController controller = (PatientDashboardController)fxmlScene.getController();
                controller.setUser(user.get());

                FXMLScene.switchScene(fxmlScene,(Node)event.getSource());
            }
            else{
                CustomAlertBox.showCustomAlert("No User Registered with this email", "error");
            }
        } else {
            // You can add error display logic here
            CustomAlertBox.showCustomAlert("Invalid verification code", "error");
        }
    }

    public void checkCode(KeyEvent keyEvent) {
    }
}
