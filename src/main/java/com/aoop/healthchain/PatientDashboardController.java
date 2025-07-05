package com.aoop.healthchain;

import com.aoop.healthchain.model.Appointment;
import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.TableCell;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PatientDashboardController implements Initializable {
    @FXML private BorderPane patientDashboardBorderPane;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label prescriptionCountLabel;
    @FXML private Label testCountLabel;
    @FXML private ListView<String> recentActivityList;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> departmentColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;

    private UserData currentUser;
    private Node originalCenterNode;

    public void setUser(UserData userData) {
        this.currentUser = userData; // Store the actual user data
        // Set user data in the dashboard
        userNameLabel.setText(userData.fullName());
        userEmailLabel.setText(userData.email());

        // Refresh data for this user
        updateStats();
        initializeRecentActivity();
        initializeAppointmentsTable();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            originalCenterNode = patientDashboardBorderPane.getCenter();
        });

        // Initialize with default data
        updateStats();
        initializeRecentActivity();
        initializeAppointmentsTable();

        // Apply custom cell factory for status column to display different styles
        statusColumn.setCellFactory(column -> {
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
    }

    private void updateStats() {
        // In a real application, fetch these from your database
        appointmentCountLabel.setText("3");
        prescriptionCountLabel.setText("5");
        testCountLabel.setText("2");
    }

    private void initializeRecentActivity() {
        // Sample activity data - in a real app, fetch this from your database
        ObservableList<String> activities = FXCollections.observableArrayList(
            "Prescription updated by Dr. Smith - 2 hours ago",
            "Lab results uploaded - Yesterday",
            "Appointment scheduled with Dr. Johnson - 2 days ago",
            "Prescription refill authorized - 3 days ago",
            "Completed checkup with Dr. Wilson - 1 week ago"
        );
        recentActivityList.setItems(activities);
    }

    private void initializeAppointmentsTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Sample appointment data - in a real app, fetch this from your database
        ObservableList<Appointment> appointments = FXCollections.observableArrayList(
            new Appointment(1, 1, 1, LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "10:00 AM", "Dr. Smith", "Cardiology", "Upcoming"),
            new Appointment(2, 1, 2, LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "02:30 PM", "Dr. Johnson", "Neurology", "Confirmed"),
            new Appointment(3, 1, 3, LocalDate.now().plusDays(8).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "11:15 AM", "Dr. Wilson", "Orthopedics", "Confirmed"),
            new Appointment(4, 1, 4, LocalDate.now().minusDays(5).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "09:00 AM", "Dr. Brown", "General Medicine", "Completed"),
            new Appointment(5, 1, 5, LocalDate.now().minusDays(10).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), "03:45 PM", "Dr. Davis", "Ophthalmology", "Cancelled")
        );
        appointmentsTable.setItems(appointments);
    }

    @FXML
    private void handleOverview() {
        if (patientDashboardBorderPane != null && originalCenterNode != null) {
            patientDashboardBorderPane.setCenter(originalCenterNode);
        }
    }

    @FXML
    private void handleAppointments() {
        try {
            if (currentUser == null) {
                System.err.println("Error: No user is logged in.");
                return;
            }
            FXMLScene fxmlScene = FXMLScene.load("appointment_page.fxml");
            PatientAppointmentController controller = (PatientAppointmentController) fxmlScene.getController();
            if (controller != null) {
                controller.setCurrentUser(currentUser);
            }
            patientDashboardBorderPane.setCenter(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading appointment page: " + e.getMessage());
        }
    }

    @FXML
    private void handleMedicalRecords() {
        System.out.println("Navigating to Medical Records");
        // In a real application, you would navigate to the medical records view
    }

    @FXML
    private void handlePrescriptions() {
        System.out.println("Navigating to Prescriptions");
        // In a real application, you would navigate to the prescriptions view
    }

    @FXML
    private void handleChat() {
        try {
            if (currentUser == null) {
                System.err.println("Error: No user is logged in.");
                return;
            }
            
            FXMLScene fxmlScene = FXMLScene.load("patient_chat.fxml");
            PatientChatController controller = (PatientChatController) fxmlScene.getController();
            if (controller != null) {
                controller.setCurrentUser(currentUser);
            }
            
            patientDashboardBorderPane.setCenter(fxmlScene.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading chat interface: " + e.getMessage());
        }
    }

    @FXML
    private void handleSettings() {
        System.out.println("Navigating to Settings");
        // In a real application, you would navigate to the settings view
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), patientDashboardBorderPane);
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error during logout: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleNewAppointment() {
        System.out.println("Creating new appointment");
        // In a real application, you would open a dialog to create a new appointment
    }
}

