package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.model.Appointment;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.layout.VBox;

public class DoctorDashboardController {

    @FXML private BorderPane doctorDashboardBorderPane;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorEmailLabel;

    @FXML
    private Label patientCountLabel;

    @FXML
    private Label appointmentCountLabel;

    @FXML
    private Label completedCountLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableView<UserData> patientsTable;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> patientNameColumn;

    @FXML
    private TableColumn<Appointment, String> purposeColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private TableColumn<Appointment, Void> actionsColumn;

    @FXML
    private TableColumn<UserData, Integer> patientIdColumn;

    @FXML
    private TableColumn<UserData, String> patientNameColumn2;

    @FXML
    private TableColumn<UserData, String> patientEmailColumn;

    @FXML
    private TableColumn<UserData, String> lastVisitColumn;

    @FXML
    private TableColumn<UserData, Void> patientActionsColumn;

    @FXML private Button dashboardButton;
    @FXML private Button patientsButton;
    @FXML private Button appointmentsButton;

    @FXML private VBox sidebarMenu;
    @FXML private Button sidebarDashboardButton;
    @FXML private Button sidebarPatientsButton;
    @FXML private Button sidebarAppointmentsButton;
    @FXML private Button sidebarChatButton;
    @FXML private Button sidebarReportsButton;
    @FXML private Button sidebarSettingsButton;
    @FXML private Button sidebarLogoutButton;

    private UserData currentUser;
    private Node originalCenterNode;
    private ObservableList<Appointment> doctorAppointments = FXCollections.observableArrayList();
    private ObservableList<UserData> myPatients = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            originalCenterNode = doctorDashboardBorderPane.getCenter();
            // Set sidebar as the left node of the BorderPane
            if (sidebarMenu != null) {
                doctorDashboardBorderPane.setLeft(sidebarMenu);
            }
        });
        
        // Only set up tables, don't load data yet
        setupAppointmentsTable();
        setupPatientsTable();
        setupSidebarActions();
        
        // Data will be loaded when setUser is called
    }

    public void setUser(UserData user) {
        this.currentUser = user;
        updateUserInfo();
        
        // Test database connection and data
        testDatabaseConnection();
        
        // Load all dashboard data after user is set
        loadDashboardData();
        loadDoctorAppointments();
        loadPatientsForDoctor();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            doctorNameLabel.setText("Dr. " + currentUser.fullName());
            doctorEmailLabel.setText(currentUser.email());
        }
    }

    private void testDatabaseConnection() {
        if (currentUser == null) return;
        
        try {
            DatabaseConnection db = new DatabaseConnection();
            System.out.println("=== DATABASE CONNECTION TEST ===");
            System.out.println("User ID: " + currentUser.id());
            System.out.println("User Name: " + currentUser.fullName());
            System.out.println("User Email: " + currentUser.email());
            
            // Test 1: Check if doctor exists
            String doctorSql = "SELECT id, specialization FROM doctors WHERE user_id = ?";
            PreparedStatement doctorStmt = db.getConnection().prepareStatement(doctorSql);
            doctorStmt.setLong(1, currentUser.id());
            ResultSet doctorRs = doctorStmt.executeQuery();
            
            if (doctorRs.next()) {
                int doctorId = doctorRs.getInt("id");
                String specialization = doctorRs.getString("specialization");
                System.out.println("Doctor found - ID: " + doctorId + ", Specialization: " + specialization);
                
                // Test 2: Check total appointments for this doctor
                String appointmentSql = "SELECT COUNT(*) as total FROM appointments WHERE doctor_id = ?";
                PreparedStatement appointmentStmt = db.getConnection().prepareStatement(appointmentSql);
                appointmentStmt.setInt(1, doctorId);
                ResultSet appointmentRs = appointmentStmt.executeQuery();
                
                if (appointmentRs.next()) {
                    int totalAppointments = appointmentRs.getInt("total");
                    System.out.println("Total appointments for doctor: " + totalAppointments);
                }
                
                // Test 3: Check today's appointments
                String todaySql = "SELECT COUNT(*) as today FROM appointments WHERE doctor_id = ? AND DATE(date) = CURDATE()";
                PreparedStatement todayStmt = db.getConnection().prepareStatement(todaySql);
                todayStmt.setInt(1, doctorId);
                ResultSet todayRs = todayStmt.executeQuery();
                
                if (todayRs.next()) {
                    int todayAppointments = todayRs.getInt("today");
                    System.out.println("Today's appointments: " + todayAppointments);
                }
                
                // Test 4: Show all appointments for this doctor
                String allAppointmentsSql = """
                    SELECT a.id, a.date, a.time, a.status, u.full_name as patient_name
                    FROM appointments a 
                    JOIN users u ON a.patient_id = u.id 
                    WHERE a.doctor_id = ? 
                    ORDER BY a.date DESC, a.time DESC
                    """;
                PreparedStatement allStmt = db.getConnection().prepareStatement(allAppointmentsSql);
                allStmt.setInt(1, doctorId);
                ResultSet allRs = allStmt.executeQuery();
                
                System.out.println("All appointments for doctor " + doctorId + ":");
                int count = 0;
                while (allRs.next()) {
                    count++;
                    System.out.println("  " + count + ". ID: " + allRs.getInt("id") + 
                                     ", Date: " + allRs.getString("date") + 
                                     ", Time: " + allRs.getString("time") + 
                                     ", Status: " + allRs.getString("status") + 
                                     ", Patient: " + allRs.getString("patient_name"));
                }
                if (count == 0) {
                    System.out.println("  No appointments found");
                }
                
            } else {
                System.out.println("ERROR: Doctor not found for user ID: " + currentUser.id());
            }
            
            System.out.println("=== END DATABASE TEST ===");
            
        } catch (Exception e) {
            System.out.println("ERROR in database test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardData() {
        if (currentUser == null) {
            System.out.println("Warning: currentUser is null, cannot load dashboard data");
            return;
        }
        
        System.out.println("Loading dashboard data for user: " + currentUser.id() + " (" + currentUser.fullName() + ")");
        
        try {
            DatabaseConnection db = new DatabaseConnection();
            
            // Get doctor ID from user ID
            int doctorId = getDoctorIdFromUser(currentUser.id());
            System.out.println("Doctor ID resolved: " + doctorId);
            
            // Load patient count for this doctor
            loadPatientCount(db, doctorId);
            
            // Load appointment counts
            loadAppointmentCounts(db, doctorId);
            
            // Load doctor specialization
            loadDoctorSpecialization(db, doctorId);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if database error
            patientCountLabel.setText("0");
            appointmentCountLabel.setText("0");
            completedCountLabel.setText("0");
            specializationLabel.setText("N/A");
        }
    }

    private void loadPatientCount(DatabaseConnection db, int doctorId) throws SQLException, ClassNotFoundException {
        String sql = """
            SELECT COUNT(DISTINCT a.patient_id) as patient_count
            FROM appointments a
            WHERE a.doctor_id = ?
            """;
        
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            patientCountLabel.setText(String.valueOf(rs.getInt("patient_count")));
        } else {
            patientCountLabel.setText("0");
        }
    }

    private void loadAppointmentCounts(DatabaseConnection db, int doctorId) throws SQLException, ClassNotFoundException {
        // Today's appointments
        String todaySql = """
            SELECT COUNT(*) as appointment_count
            FROM appointments a
            WHERE a.doctor_id = ? AND DATE(a.date) = CURDATE()
            """;
        
        PreparedStatement todayStmt = db.getConnection().prepareStatement(todaySql);
        todayStmt.setInt(1, doctorId);
        ResultSet todayRs = todayStmt.executeQuery();
        
        if (todayRs.next()) {
            appointmentCountLabel.setText(String.valueOf(todayRs.getInt("appointment_count")));
        } else {
            appointmentCountLabel.setText("0");
        }
        
        // Completed appointments today
        String completedSql = """
            SELECT COUNT(*) as completed_count
            FROM appointments a
            WHERE a.doctor_id = ? AND DATE(a.date) = CURDATE() AND a.status = 'completed'
            """;
        
        PreparedStatement completedStmt = db.getConnection().prepareStatement(completedSql);
        completedStmt.setInt(1, doctorId);
        ResultSet completedRs = completedStmt.executeQuery();
        
        if (completedRs.next()) {
            completedCountLabel.setText(String.valueOf(completedRs.getInt("completed_count")));
        } else {
            completedCountLabel.setText("0");
        }
    }

    private void loadDoctorSpecialization(DatabaseConnection db, int doctorId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT specialization FROM doctors WHERE id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String specialization = rs.getString("specialization");
            specializationLabel.setText(specialization != null ? specialization : "N/A");
        } else {
            specializationLabel.setText("N/A");
        }
    }

    private int getDoctorIdFromUser(Long userId) throws SQLException, ClassNotFoundException {
        DatabaseConnection db = new DatabaseConnection();
        String sql = "SELECT id FROM doctors WHERE user_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1; // Return -1 if doctor not found
    }

    @FXML
    void handleDashboard() {
        if (doctorDashboardBorderPane != null && originalCenterNode != null) {
            doctorDashboardBorderPane.setCenter(originalCenterNode);
        }
        loadDashboardData();
    }

    @FXML
    void handlePatients() {
        // Show the patients table in the center of the dashboard
        doctorDashboardBorderPane.setCenter(patientsTable);
        loadPatientsForDoctor();
    }

    @FXML
    void handleAppointments() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("doctor_appointments.fxml");
            DoctorAppointmentsController controller = (DoctorAppointmentsController) fxmlScene.getController();
            if (controller != null && currentUser != null) {
                controller.setDoctor(currentUser);
            }
            // Set the appointments view in the center, keep sidebar
            doctorDashboardBorderPane.setCenter(fxmlScene.getRoot());
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error loading appointments portal: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleMyAppointments() {
        try {
            // Show the appointments table in the center of the dashboard
            doctorDashboardBorderPane.setCenter(appointmentsTable);
            loadDoctorAppointments();
            CustomAlertBox.showCustomAlert("Showing your appointments", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error loading my appointments: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handlePrescriptions() {
        try {
            // TODO: Navigate to prescriptions page
            CustomAlertBox.showCustomAlert("Prescriptions page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to prescriptions page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleChat() {
        try {
            // Load the doctor chat interface
            FXMLScene fxmlScene = FXMLScene.load("doctor_chat.fxml");
            DoctorChatController controller = (DoctorChatController) fxmlScene.getController();
            if (controller != null) {
                controller.setCurrentDoctor(currentUser);
            }
            
            doctorDashboardBorderPane.setCenter(fxmlScene.getRoot());
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error loading chat interface: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleReports() {
        try {
            // TODO: Navigate to reports page
            CustomAlertBox.showCustomAlert("Reports page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to reports page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleSettings() {
        try {
            // TODO: Navigate to settings page
            CustomAlertBox.showCustomAlert("Settings page coming soon!", "info");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error navigating to settings page: " + e.getMessage(), "error");
        }
    }

    @FXML
    void handleAddAppointment() {
        // TODO: Show dialog to add new appointment (not implemented here)
        CustomAlertBox.showCustomAlert("Add appointment feature coming soon!", "info");
    }

    @FXML
    void handleRefresh() {
        if (currentUser != null) {
            System.out.println("Manual refresh triggered");
            loadDashboardData();
            loadDoctorAppointments();
            loadPatientsForDoctor();
            CustomAlertBox.showCustomAlert("Dashboard data refreshed!", "success");
        } else {
            CustomAlertBox.showCustomAlert("No user logged in!", "error");
        }
    }

    @FXML
    void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), doctorNameLabel);
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error during logout: " + e.getMessage(), "error");
        }
    }

    private void loadDoctorAppointments() {
        doctorAppointments.clear();
        if (currentUser == null) {
            System.out.println("Warning: currentUser is null in loadDoctorAppointments");
            return;
        }
        
        try {
            // Get doctor ID from user ID
            int doctorId = getDoctorIdFromUser(currentUser.id());
            System.out.println("Doctor ID for user " + currentUser.id() + ": " + doctorId);
            
            if (doctorId == -1) {
                System.out.println("Warning: Doctor not found for user ID: " + currentUser.id());
                return;
            }
            
            DatabaseConnection db = new DatabaseConnection();
            String sql = """
                SELECT a.id, a.time, u.full_name as patient_name, a.department as purpose, a.status, a.date
                FROM appointments a 
                JOIN users u ON a.patient_id = u.id 
                WHERE a.doctor_id = ? 
                ORDER BY a.date DESC, a.time DESC
                """;
            PreparedStatement stmt = db.getConnection().prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            int appointmentCount = 0;
            while (rs.next()) {
                appointmentCount++;
                doctorAppointments.add(new Appointment(
                    rs.getInt("id"),
                    0, // patientId not needed here
                    doctorId,
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("patient_name"),
                    rs.getString("purpose"),
                    rs.getString("status")
                ));
            }
            
            System.out.println("Loaded " + appointmentCount + " appointments for doctor ID: " + doctorId);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading appointments: " + e.getMessage(), "error");
        }
        appointmentsTable.setItems(doctorAppointments);
    }

    private void setupAppointmentsTable() {
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        patientNameColumn.setCellValueFactory(cellData -> cellData.getValue().doctorProperty()); // doctorProperty used for patient name here
        purposeColumn.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        // Actions column
        actionsColumn.setCellFactory(getActionsCellFactory());
    }

    private Callback<TableColumn<Appointment, Void>, TableCell<Appointment, Void>> getActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirm");
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pane = new HBox(8, confirmBtn, completeBtn, cancelBtn);
            {
                // Add visual styles for clarity
                confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                completeBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                cancelBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                confirmBtn.setPrefWidth(70);
                completeBtn.setPrefWidth(70);
                cancelBtn.setPrefWidth(70);

                confirmBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Confirmed"));
                completeBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Completed"));
                cancelBtn.setOnAction(e -> updateAppointmentStatus(getTableView().getItems().get(getIndex()), "Cancelled"));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private void updateAppointmentStatus(Appointment appointment, String newStatus) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "UPDATE appointments SET status = ? WHERE id = ?";
            PreparedStatement stmt = db.getConnection().prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, appointment.getId());
            stmt.executeUpdate();
            appointment.setStatus(newStatus);
            appointmentsTable.refresh();
            CustomAlertBox.showCustomAlert("Appointment status updated to " + newStatus, "success");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error updating status: " + e.getMessage(), "error");
        }
    }

    private void setupPatientsTable() {
        // Set up columns
        patientIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().id().intValue()).asObject());
        patientNameColumn2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().fullName()));
        patientEmailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().email()));
        
        // Last visit column - will be populated from appointment data
        lastVisitColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("N/A"));
        
        // Add a "View" button to patientActionsColumn
        patientActionsColumn.setCellFactory(col -> new TableCell<UserData, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                viewBtn.setPrefWidth(60);
                viewBtn.setOnAction(e -> {
                    UserData patient = getTableView().getItems().get(getIndex());
                    showPatientDashboard(patient);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        patientsTable.setItems(myPatients);
    }

    private void showPatientDashboard(UserData patient) {
        try {
            FXMLScene fxmlScene = FXMLScene.load("patient_profile.fxml");
            PatientProfileController controller = (PatientProfileController) fxmlScene.getController();
            if (controller != null) {
                controller.setUser(patient);
            }
            doctorDashboardBorderPane.setCenter(fxmlScene.getRoot());
        } catch (Exception e) {
            CustomAlertBox.showCustomAlert("Error loading patient profile: " + e.getMessage(), "error");
        }
    }

    private void loadPatientsForDoctor() {
        myPatients.clear();
        if (currentUser == null) {
            System.out.println("Warning: currentUser is null in loadPatientsForDoctor");
            return;
        }
        
        try {
            // Get doctor ID from user ID
            int doctorId = getDoctorIdFromUser(currentUser.id());
            System.out.println("Loading patients for doctor ID: " + doctorId);
            
            if (doctorId == -1) {
                System.out.println("Warning: Doctor not found for user ID: " + currentUser.id());
                return;
            }
            
            DatabaseConnection db = new DatabaseConnection();
            String sql = """
                SELECT DISTINCT u.id, u.full_name, u.email, u.role,
                       MAX(a.date) as last_visit
                FROM appointments a 
                JOIN users u ON a.patient_id = u.id 
                WHERE a.doctor_id = ? 
                GROUP BY u.id, u.full_name, u.email, u.role
                ORDER BY last_visit DESC
                """;
            
            PreparedStatement stmt = db.getConnection().prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            int patientCount = 0;
            while (rs.next()) {
                patientCount++;
                myPatients.add(new UserData(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    "", // phone (not available)
                    "", // bloodGroup
                    "", // password
                    rs.getString("role"),
                    true // isVerified (default to true)
                ));
            }
            
            System.out.println("Loaded " + patientCount + " patients for doctor ID: " + doctorId);
            
            if (patientCount == 0) {
                CustomAlertBox.showCustomAlert("No patients found who have booked with you.", "info");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading patients: " + e.getMessage(), "error");
        }
        patientsTable.setItems(myPatients);
    }

    private void setupSidebarActions() {
        if (sidebarDashboardButton != null) sidebarDashboardButton.setOnAction(e -> handleDashboard());
        if (sidebarPatientsButton != null) sidebarPatientsButton.setOnAction(e -> handlePatients());
        if (sidebarAppointmentsButton != null) sidebarAppointmentsButton.setOnAction(e -> handleAppointments());
        if (sidebarChatButton != null) sidebarChatButton.setOnAction(e -> handleChat());
        if (sidebarReportsButton != null) sidebarReportsButton.setOnAction(e -> handleReports());
        if (sidebarSettingsButton != null) sidebarSettingsButton.setOnAction(e -> handleSettings());
        if (sidebarLogoutButton != null) sidebarLogoutButton.setOnAction(e -> handleLogout());
    }
}