package com.aoop.healthchain.util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlertBox {
    public static void showCustomAlert(String message, String type) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UNDECORATED);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        javafx.scene.control.Button okButton = new javafx.scene.control.Button("OK");
        okButton.setStyle("""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-padding: 5px 20px;
            -fx-cursor: hand;
        """);
        okButton.setOnAction(e -> alertStage.close());

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(messageLabel, okButton);

        // Set different styles based on alert type
        String backgroundColor = type.equals("error") ? "#ffebee" : "#e8f5e9";
        String borderColor = type.equals("error") ? "#ef5350" : "#66bb6a";

        layout.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-padding: 20px;
            -fx-border-color: %s;
            -fx-border-width: 2px;
            -fx-border-radius: 5px;
            -fx-background-radius: 5px;
        """, backgroundColor, borderColor));

        Scene scene = new Scene(layout);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }
}
