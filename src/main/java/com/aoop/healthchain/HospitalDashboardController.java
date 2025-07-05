package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.DatabaseConnection;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class HospitalDashboardController implements Initializable {
    @FXML private Label hospitalNameLabel;
    @FXML private Label hospitalEmailLabel;
    @FXML private Label doctorCountLabel;
    @FXML private Label patientCountLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label departmentCountLabel;

    // New Analytics Fields
    @FXML private Label totalDoctorsLabel;
    @FXML private Label activeDoctorsLabel;
    @FXML private Label specializationCountLabel;
    @FXML private Label recentDoctorsLabel;
    @FXML private BarChart<String, Number> specializationChart;
    @FXML private LineChart<String, Number> patientGrowthChart;
    @FXML private CategoryAxis specializationAxis;
    @FXML private NumberAxis doctorCountAxis;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis patientCountAxis;

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> patientNameColumn;
    @FXML private TableColumn<Appointment, String> doctorNameColumn;
    @FXML private TableColumn<Appointment, String> purposeColumn;
    @FXML private TableColumn<Appointment, String> appointmentStatusColumn;
    @FXML private TableColumn<Appointment, Void> appointmentActionsColumn;

    private UserData userData;

    public void setUser(UserData userData) {
        this.userData = userData;
        
        if (userData != null) {
            // Update hospital labels
            hospitalNameLabel.setText(userData.fullName());
            hospitalEmailLabel.setText(userData.email());
            
            // Load dashboard data
            loadData();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize appointment table
        initializeAppointmentsTable();
        
        // Don't load data here - wait for userData to be set
        // Data will be loaded when setUser is called
    }

    private void initializeAppointmentsTable() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        appointmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Style the status column
        appointmentStatusColumn.setCellFactory(column -> {
            return new TableCell<Appointment, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item);

                        // Apply different style classes based on status
                        getStyleClass().removeAll("status-upcoming", "status-confirmed", "status-completed", "status-cancelled");

                        switch (item.toLowerCase()) {
                            case "upcoming":
                                getStyleClass().add("status-upcoming");
                                break;
                            case "confirmed":
                                getStyleClass().add("status-confirmed");
                                break;
                            case "completed":
                                getStyleClass().add("status-completed");
                                break;
                            case "cancelled":
                                getStyleClass().add("status-cancelled");
                                break;
                        }
                    }
                }
            };
        });

        // Configure the action column with buttons
        appointmentActionsColumn.setCellFactory(column -> {
            return new TableCell<Appointment, Void>() {
                private final Button confirmButton = new Button("Confirm");
                private final Button cancelButton = new Button("Cancel");
                private final HBox pane = new HBox(5, confirmButton, cancelButton);

                {
                    confirmButton.getStyleClass().add("confirm-button");
                    cancelButton.getStyleClass().add("cancel-button");

                    confirmButton.setOnAction(event -> {
                        Appointment appointment = getTableRow().getItem();
                        if (appointment != null) {
                            handleConfirmAppointment(appointment);
                        }
                    });

                    cancelButton.setOnAction(event -> {
                        Appointment appointment = getTableRow().getItem();
                        if (appointment != null) {
                            handleCancelAppointment(appointment);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        Appointment appointment = getTableRow().getItem();
                        if (appointment != null && appointment.getStatus().equalsIgnoreCase("upcoming")) {
                            setGraphic(pane);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
        });
    }

    private void loadData() {
        // Only load data if userData is available
        if (userData == null) {
            System.out.println("Warning: userData is null, cannot load dashboard data");
            return;
        }
        
        // Load counts for dashboard stats
        loadDashboardStats();
        
        // Load doctor analytics
        loadDoctorAnalytics();
        
        // Load appointment data
        loadAppointmentData();
    }

    private void loadDashboardStats() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            
            // Get hospital ID for current user
            int hospitalId = getHospitalIdFromUser(userData);
            
            // Total doctors for this hospital
            String doctorCountSql = "SELECT COUNT(*) FROM doctors WHERE hospital_id = ?";
            PreparedStatement doctorStmt = db.getConnection().prepareStatement(doctorCountSql);
            doctorStmt.setInt(1, hospitalId);
            ResultSet doctorRs = doctorStmt.executeQuery();
            if (doctorRs.next()) {
                doctorCountLabel.setText(String.valueOf(doctorRs.getInt(1)));
            }
            
            // Total patients (users with PATIENT role)
            ResultSet patientRs = db.queryData("SELECT COUNT(*) FROM users WHERE role = 'PATIENT'");
            if (patientRs.next()) {
                patientCountLabel.setText(String.valueOf(patientRs.getInt(1)));
            }
            
            // Today's appointments
            String appointmentSql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = CURDATE()";
            ResultSet appointmentRs = db.queryData(appointmentSql);
            if (appointmentRs.next()) {
                appointmentCountLabel.setText(String.valueOf(appointmentRs.getInt(1)));
            }
            
            // Department count (unique specializations)
            String deptSql = "SELECT COUNT(DISTINCT specialization) FROM doctors WHERE hospital_id = ? AND specialization IS NOT NULL";
            PreparedStatement deptStmt = db.getConnection().prepareStatement(deptSql);
            deptStmt.setInt(1, hospitalId);
            ResultSet deptRs = deptStmt.executeQuery();
            if (deptRs.next()) {
                departmentCountLabel.setText(String.valueOf(deptRs.getInt(1)));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if database error
            doctorCountLabel.setText("0");
            patientCountLabel.setText("0");
            appointmentCountLabel.setText("0");
            departmentCountLabel.setText("0");
        }
    }

    private void loadDoctorAnalytics() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            int hospitalId = getHospitalIdFromUser(userData);
            
            // Load metrics
            loadDoctorMetrics(db, hospitalId);
            
            // Load charts
            loadSpecializationChart(db, hospitalId);
            loadPatientGrowthChart(db, hospitalId);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if database error
            totalDoctorsLabel.setText("0");
            activeDoctorsLabel.setText("0");
            specializationCountLabel.setText("0");
            recentDoctorsLabel.setText("0");
        }
    }

    private void loadDoctorMetrics(DatabaseConnection db, int hospitalId) throws Exception {
        // Total doctors
        String totalSql = "SELECT COUNT(*) FROM doctors WHERE hospital_id = ?";
        PreparedStatement totalStmt = db.getConnection().prepareStatement(totalSql);
        totalStmt.setInt(1, hospitalId);
        ResultSet totalRs = totalStmt.executeQuery();
        if (totalRs.next()) {
            totalDoctorsLabel.setText(String.valueOf(totalRs.getInt(1)));
        }
        
        // Active doctors
        String activeSql = "SELECT COUNT(*) FROM doctors WHERE hospital_id = ? AND status = 'ACTIVE'";
        PreparedStatement activeStmt = db.getConnection().prepareStatement(activeSql);
        activeStmt.setInt(1, hospitalId);
        ResultSet activeRs = activeStmt.executeQuery();
        if (activeRs.next()) {
            activeDoctorsLabel.setText(String.valueOf(activeRs.getInt(1)));
        }
        
        // Specialization count
        String specSql = "SELECT COUNT(DISTINCT specialization) FROM doctors WHERE hospital_id = ? AND specialization IS NOT NULL";
        PreparedStatement specStmt = db.getConnection().prepareStatement(specSql);
        specStmt.setInt(1, hospitalId);
        ResultSet specRs = specStmt.executeQuery();
        if (specRs.next()) {
            specializationCountLabel.setText(String.valueOf(specRs.getInt(1)));
        }
        
        // Recent doctors (this month)
        String recentSql = "SELECT COUNT(*) FROM doctors WHERE hospital_id = ? AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())";
        PreparedStatement recentStmt = db.getConnection().prepareStatement(recentSql);
        recentStmt.setInt(1, hospitalId);
        ResultSet recentRs = recentStmt.executeQuery();
        if (recentRs.next()) {
            recentDoctorsLabel.setText(String.valueOf(recentRs.getInt(1)));
        }
    }

    private void loadSpecializationChart(DatabaseConnection db, int hospitalId) throws Exception {
        specializationChart.getData().clear();
        
        String sql = "SELECT specialization, COUNT(*) as count FROM doctors WHERE hospital_id = ? AND specialization IS NOT NULL GROUP BY specialization ORDER BY count DESC";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, hospitalId);
        ResultSet rs = stmt.executeQuery();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doctors by Specialization");
        
        while (rs.next()) {
            String specialization = rs.getString("specialization");
            int count = rs.getInt("count");
            series.getData().add(new XYChart.Data<>(specialization, count));
        }
        
        specializationChart.getData().add(series);
        
        // Add tooltips
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " doctors");
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    private void loadPatientGrowthChart(DatabaseConnection db, int hospitalId) throws Exception {
        patientGrowthChart.getData().clear();
        
        // Query to get patient growth by month for the last 12 months
        String sql = """
            SELECT 
                DATE_FORMAT(created_at, '%Y-%m') as month,
                COUNT(*) as patient_count
            FROM users 
            WHERE role = 'PATIENT' 
            AND created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(created_at, '%Y-%m')
            ORDER BY month
            """;
        
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Growth by Month");
        
        // Month names for better display
        String[] monthNames = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        
        while (rs.next()) {
            String monthYear = rs.getString("month");
            int count = rs.getInt("patient_count");
            
            // Format month for display (e.g., "2024-01" -> "Jan 2024")
            String[] parts = monthYear.split("-");
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[1]) - 1; // Convert to 0-based index
                int year = Integer.parseInt(parts[0]);
                String displayMonth = monthNames[month] + " " + year;
                series.getData().add(new XYChart.Data<>(displayMonth, count));
            }
        }
        
        patientGrowthChart.getData().add(series);
        
        // Add tooltips
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " new patients");
            Tooltip.install(data.getNode(), tooltip);
        }
        
        // If no data, add some sample data for demonstration
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("Jan 2024", 15));
            series.getData().add(new XYChart.Data<>("Feb 2024", 22));
            series.getData().add(new XYChart.Data<>("Mar 2024", 18));
            series.getData().add(new XYChart.Data<>("Apr 2024", 25));
            series.getData().add(new XYChart.Data<>("May 2024", 30));
            series.getData().add(new XYChart.Data<>("Jun 2024", 28));
        }
    }

    private void loadAppointmentData() {
        // Sample appointment data - in a real app, fetch this from your database
        ObservableList<Appointment> appointments = FXCollections.observableArrayList(
            new Appointment("09:00 AM", "Alice Johnson", "Dr. John Smith", "Routine Checkup", "Upcoming"),
            new Appointment("10:30 AM", "Bob Williams", "Dr. Jane Doe", "Consultation", "Confirmed"),
            new Appointment("11:15 AM", "Carol Davis", "Dr. Robert Johnson", "Follow-up", "Upcoming"),
            new Appointment("01:45 PM", "David Miller", "Dr. Maria Garcia", "Vaccination", "Confirmed"),
            new Appointment("03:30 PM", "Eve Wilson", "Dr. William Brown", "Initial Consultation", "Upcoming")
        );
        appointmentsTable.setItems(appointments);
    }

    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard section selected");
    }

    @FXML
    private void handleDoctors() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("doctor_list.fxml");
            DoctorListController controller = (DoctorListController) fxmlScene.getController();
            if (controller != null) {
            controller.setUser(userData);
                // Pass the hospital ID to the doctor list controller
                if (userData != null) {
                    controller.setHospitalId(getHospitalIdFromUser(userData));
                }
            FXMLScene.switchScene(fxmlScene, hospitalNameLabel.getScene().getRoot());
            } else {
                CustomAlertBox.showCustomAlert("Error: Could not load doctor list controller", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error loading doctor list: " + e.getMessage(), "error");
        }
    }

    private int getHospitalIdFromUser(UserData userData) {
        if (userData == null) {
            System.out.println("Warning: userData is null, returning default hospital ID");
            return 1; // Default hospital ID
        }
        
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT id FROM hospitals WHERE user_id = ?";
            PreparedStatement stmt = db.getConnection().prepareStatement(sql);
            stmt.setInt(1, userData.id().intValue());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Default hospital ID if not found
    }

    @FXML
    private void handlePatients() {
        System.out.println("Patients section selected");
    }

    @FXML
    private void handleAppointments() {
        System.out.println("Appointments section selected");
    }

    @FXML
    private void handleDepartments() {
        System.out.println("Departments section selected");
    }

    @FXML
    private void handleReports() {
        System.out.println("Reports section selected");
    }

    @FXML
    private void handleSettings() {
        System.out.println("Settings section selected");
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), hospitalNameLabel.getScene().getRoot());
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error during logout: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleAddDoctor() {
        System.out.println("Add doctor button clicked");
        // In a real app, you would open a dialog to add a new doctor
    }

    private void handleConfirmAppointment(Appointment appointment) {
        System.out.println("Confirm appointment: " + appointment.getPatientName() + " with " + appointment.getDoctorName());
        // In a real app, you would update the appointment status in the database
    }

    private void handleCancelAppointment(Appointment appointment) {
        System.out.println("Cancel appointment: " + appointment.getPatientName() + " with " + appointment.getDoctorName());
        // In a real app, you would update the appointment status in the database
    }

    // Helper classes for TableView
    public static class Doctor {
        private final Integer id;
        private final String name;
        private final String specialization;
        private final String department;
        private final String contact;
        private final String status;

        public Doctor(Integer id, String name, String specialization, String department, String contact, String status) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.department = department;
            this.contact = contact;
            this.status = status;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }
        public String getDepartment() { return department; }
        public String getContact() { return contact; }
        public String getStatus() { return status; }
    }

    public static class Appointment {
        private final String time;
        private final String patientName;
        private final String doctorName;
        private final String purpose;
        private final String status;

        public Appointment(String time, String patientName, String doctorName, String purpose, String status) {
            this.time = time;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.purpose = purpose;
            this.status = status;
        }

        public String getTime() { return time; }
        public String getPatientName() { return patientName; }
        public String getDoctorName() { return doctorName; }
        public String getPurpose() { return purpose; }
        public String getStatus() { return status; }
    }
}
