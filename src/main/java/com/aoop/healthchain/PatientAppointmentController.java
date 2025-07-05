package com.aoop.healthchain;

import com.aoop.healthchain.model.Appointment;
import com.aoop.healthchain.model.DoctorData;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientAppointmentController implements Initializable {
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> departmentColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, String> doctorNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> doctorDepartmentColumn;
    
    @FXML private Label selectedDoctorLabel;
    @FXML private TextField departmentField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private Button bookButton;

    private UserData currentUser;
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private Doctor selectedDoctor;

    public void setCurrentUser(UserData user) {
        this.currentUser = user;
        System.out.println("Setting current user: " + (user != null ? user.fullName() : "null"));
        
        // Refresh data when user is set
        Platform.runLater(() -> {
            loadAppointments();
            loadDoctors(); // Reload doctors to ensure fresh data
            setupTimeSlots();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PatientAppointmentController initializing...");
        setupAppointmentsTable();
        setupDoctorsTable();
        setupEventHandlers();
        setupTimeSlots();
        
        // Load doctors immediately when page initializes
        Platform.runLater(() -> {
            loadDoctors();
            System.out.println("Initialization completed - doctors loaded: " + doctors.size());
        });
    }

    private void setupAppointmentsTable() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        doctorColumn.setCellValueFactory(cellData -> cellData.getValue().doctorProperty());
        departmentColumn.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        appointmentsTable.setItems(appointments);
    }

    private void setupDoctorsTable() {
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        doctorDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        doctorsTable.setItems(doctors);
        
        // Add context menu for refresh
        ContextMenu contextMenu = new ContextMenu();
        MenuItem refreshItem = new MenuItem("Refresh Doctors List");
        refreshItem.setOnAction(e -> refreshDoctors());
        contextMenu.getItems().add(refreshItem);
        doctorsTable.setContextMenu(contextMenu);
    }

    private void setupEventHandlers() {
        doctorsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedDoctor = newValue;
                selectedDoctorLabel.setText(newValue.getName());
                departmentField.setText(newValue.getDepartment());
                System.out.println("Selected doctor: " + newValue.getName());
            }
        });

        bookButton.setOnAction(e -> bookAppointment());
    }

    private void setupTimeSlots() {
        timeComboBox.getItems().clear();
        // Add time slots from 9 AM to 5 PM
        for (int hour = 9; hour <= 17; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
            if (hour < 17) {
                timeComboBox.getItems().add(String.format("%02d:30", hour));
            }
        }
    }

    private void loadAppointments() {
        appointments.clear();
        if (currentUser == null) return;
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String sql = "SELECT a.id, a.patient_id, a.doctor_id, a.date, a.time, u.full_name AS doctor, a.department, a.status " +
                         "FROM appointments a JOIN doctors d ON a.doctor_id = d.id JOIN users u ON d.user_id = u.id " +
                         "WHERE a.patient_id = ? ORDER BY a.date DESC, a.time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.id().intValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("doctor"),
                    rs.getString("department"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading appointments: " + e.getMessage());
        }
    }

    private void loadDoctors() {
        System.out.println("Loading doctors...");
        doctors.clear();
        try (Connection conn = new DatabaseConnection().getConnection()) {
            System.out.println("Database connection established");
            
            // Test if doctors table exists
            testDatabaseConnection(conn);
            
            // First try a simple query to see if doctors table has data
            System.out.println("Testing simple doctors query...");
            Statement testStmt = conn.createStatement();
            ResultSet testRs = testStmt.executeQuery("SELECT COUNT(*) FROM doctors");
            testRs.next();
            int doctorCount = testRs.getInt(1);
            System.out.println("Found " + doctorCount + " doctors in database");
            
            if (doctorCount == 0) {
                System.out.println("No doctors found in database!");
                return;
            }
            
            // Now try the full query
            String sql = "SELECT d.id, u.full_name as name, d.specialization, d.specialization as department " +
                         "FROM doctors d JOIN users u ON d.user_id = u.id " +
                         "ORDER BY u.full_name ASC";
            System.out.println("Executing SQL: " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
                Doctor doctor = new Doctor(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("department")
                );
                doctors.add(doctor);
                System.out.println("Added doctor: " + doctor.getName() + " (ID: " + doctor.getId() + ")");
            }
            System.out.println("Total doctors loaded: " + count);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading doctors: " + e.getMessage());
            showAlert("Error loading doctors: " + e.getMessage());
        }
    }
    
    private void testDatabaseConnection(Connection conn) {
        try {
            // Check if doctors table exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "doctors", null);
            if (tables.next()) {
                System.out.println("Doctors table exists");
                
                // Check table structure
                ResultSet columns = metaData.getColumns(null, null, "doctors", null);
                System.out.println("Doctors table columns:");
                while (columns.next()) {
                    System.out.println("  - " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
                }
                
                // Count doctors
                Statement stmt = conn.createStatement();
                ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM doctors");
                countRs.next();
                System.out.println("Total doctors in database: " + countRs.getInt(1));
                
            } else {
                System.out.println("Doctors table does not exist!");
            }
        } catch (Exception e) {
            System.err.println("Error testing database: " + e.getMessage());
        }
    }

    private void bookAppointment() {
        if (currentUser == null) {
            showAlert("No user logged in.");
            return;
        }
        if (selectedDoctor == null) {
            showAlert("Please select a doctor.");
            return;
        }
        String department = departmentField.getText();
        LocalDate date = datePicker.getValue();
        String time = timeComboBox.getValue();
        
        if (department.isEmpty() || date == null || time == null) {
            showAlert("Please fill all fields.");
            return;
        }
        
        if (date.isBefore(LocalDate.now())) {
            showAlert("Cannot book appointment for past dates.");
            return;
        }

        try (Connection conn = new DatabaseConnection().getConnection()) {
            // Check if the time slot is available
            String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND date = ? AND time = ? AND status != 'cancelled'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedDoctor.getId());
            checkStmt.setDate(2, Date.valueOf(date));
            checkStmt.setString(3, time);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            if (checkRs.getInt(1) > 0) {
                showAlert("This time slot is already booked. Please select another time.");
                return;
            }

            String sql = "INSERT INTO appointments (patient_id, doctor_id, date, time, department, status) VALUES (?, ?, ?, ?, ?, 'upcoming')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.id().intValue());
            stmt.setInt(2, selectedDoctor.getId());
            stmt.setDate(3, Date.valueOf(date));
            stmt.setString(4, time);
            stmt.setString(5, department);
            stmt.executeUpdate();
            
            showAlert("Appointment booked successfully!");
            clearForm();
            loadAppointments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to book appointment: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedDoctor = null;
        selectedDoctorLabel.setText("None selected");
        departmentField.clear();
        datePicker.setValue(null);
        timeComboBox.setValue(null);
        doctorsTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Booking");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public method to refresh doctors list
    public void refreshDoctors() {
        System.out.println("Refreshing doctors list...");
        loadDoctors();
    }

    // Simple Doctor class for TableView
    public static class Doctor {
        private final int id;
        private final String name;
        private final String specialization;
        private final String department;

        public Doctor(int id, String name, String specialization, String department) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.department = department;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }
        public String getDepartment() { return department; }
    }
} 