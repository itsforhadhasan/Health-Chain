module com.aoop.healthchain {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.dotenv;
    requires java.mail;
    requires java.prefs;
    requires java.desktop;


    opens com.aoop.healthchain to javafx.fxml;
    exports com.aoop.healthchain;
    exports com.aoop.healthchain.auth;
    opens com.aoop.healthchain.auth to javafx.fxml;
    exports com.aoop.healthchain.model;
    opens com.aoop.healthchain.model to javafx.fxml;
}