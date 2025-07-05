package com.aoop.healthchain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.model.HospitalData;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class AdminViewHospitalController implements Initializable {

    public static int selectedHospitalId = -1;

    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userEmailLabel;
    
    @FXML
    private Label hospitalNameLarge;
    
    @FXML
    private Label detailValue;
    
    @FXML
    private Label statusBadge;
    
    @FXML
    private Label detailValueEmail;
    
    @FXML
    private Label detailValuePhone;
    
    @FXML
    private Label detailValueAddress;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TableView<DoctorData> doctorsTable;
    
    @FXML
    private TableColumn<DoctorData, String> doctorIdColumn;
    
    @FXML
    private TableColumn<DoctorData, String> doctorNameColumn;
    
    @FXML
    private TableColumn<DoctorData, String> specializationColumn;
    
    @FXML
    private TableColumn<DoctorData, String> emailColumn;
    
    @FXML
    private TableColumn<DoctorData, String> phoneColumn;
    
    @FXML
    private TableColumn<DoctorData, String> statusColumn;

    @FXML
    private TableColumn<DoctorData, Void> actionsColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUserInfo();
        setupDoctorsTable();
        loadHospitalData();
        loadDoctorsForHospital();
    }

    private void setupUserInfo() {
        // Set dummy user information
        userNameLabel.setText("Admin User");
        userEmailLabel.setText("admin@healthchain.com");
    }

    private void setupDoctorsTable() {
        // Configure table columns
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Configure the actions column with view, edit, and remove buttons
        actionsColumn.setCellFactory(column -> {
            return new TableCell<DoctorData, Void>() {
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");
                private final Button removeButton = new Button("Remove");
                private final HBox pane = new HBox(5, viewButton, editButton, removeButton);

                {
                    // Style the buttons
                    viewButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12;");
                    editButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12;");
                    removeButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12;");

                    viewButton.setOnAction(event -> {
                        DoctorData doctor = getTableRow().getItem();
                        if (doctor != null) {
                            handleViewDoctor(doctor);
                        }
                    });

                    editButton.setOnAction(event -> {
                        DoctorData doctor = getTableRow().getItem();
                        if (doctor != null) {
                            handleEditDoctor(doctor);
                        }
                    });

                    removeButton.setOnAction(event -> {
                        DoctorData doctor = getTableRow().getItem();
                        if (doctor != null) {
                            handleRemoveDoctor(doctor);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        });
    }

    private void loadHospitalData() {
        if (selectedHospitalId <= 0) return;
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT * FROM hospitals WHERE id=" + selectedHospitalId;
            ResultSet rs = db.queryData(sql);
            if (rs.next()) {
                hospitalNameLarge.setText(rs.getString("name"));
                detailValue.setText("H" + String.format("%03d", rs.getInt("id")));
                statusBadge.setText(rs.getString("status"));
                detailValueEmail.setText(rs.getString("email"));
                detailValuePhone.setText(rs.getString("phone"));
                detailValueAddress.setText(rs.getString("address") + ", " + rs.getString("city") + ", " + rs.getString("zip"));
                descriptionArea.setText(rs.getString("website")); // or use a description field if available
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDoctorsForHospital() {
        // TODO: Query doctors for this hospital and populate the table
    }

    @FXML
    private void handleBackToList() {
        // Get the current scene's root node to pass to switchScene
        Node root = userNameLabel.getScene().getRoot();
        FXMLScene.switchScene("hospital_list.fxml", root);
    }

    @FXML
    private void handleAddDoctor() {
        // TODO: Implement add doctor functionality
        System.out.println("Add Doctor clicked");
    }

    @FXML
    private void handleViewDoctor(DoctorData doctor) {
        // TODO: Implement view doctor functionality
        System.out.println("View Doctor: " + doctor.name());
    }

    @FXML
    private void handleEditDoctor(DoctorData doctor) {
        // TODO: Implement edit doctor functionality
        System.out.println("Edit Doctor: " + doctor.name());
    }

    @FXML
    private void handleRemoveDoctor(DoctorData doctor) {
        // TODO: Implement remove doctor functionality
        System.out.println("Remove Doctor: " + doctor.name());
    }

    @FXML
    private void handleDashboard() {
        Node root = userNameLabel.getScene().getRoot();
        FXMLScene.switchScene("admin_dashboard.fxml", root);
    }

    @FXML
    private void handleHospitals() {
        Node root = userNameLabel.getScene().getRoot();
        FXMLScene.switchScene("hospital_list.fxml", root);
    }

    @FXML
    private void handleDoctors() {
        Node root = userNameLabel.getScene().getRoot();
        FXMLScene.switchScene("admin-doctorlist.fxml", root);
    }

    @FXML
    private void handleSettings() {
        // TODO: Implement settings functionality
        System.out.println("Settings clicked");
    }

    @FXML
    private void handleLogout() {
        Node root = userNameLabel.getScene().getRoot();
        FXMLScene.switchScene("auth/login.fxml", root);
    }
} 