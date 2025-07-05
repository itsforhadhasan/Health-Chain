package com.aoop.healthchain;

import com.aoop.healthchain.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException {
        DatabaseConnection db = new DatabaseConnection();
        Connection connection = db.getConnection();
        System.out.println("Connected to database: " + connection.getCatalog());
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("auth/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),1000,600);
        stage.setTitle("Health Chain!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}