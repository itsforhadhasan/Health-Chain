package com.aoop.healthchain;

import com.aoop.healthchain.model.UserData;
import com.aoop.healthchain.util.CustomAlertBox;
import com.aoop.healthchain.util.FXMLScene;
import com.aoop.healthchain.util.LoginInfoSave;
import com.aoop.healthchain.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userCountLabel;
    @FXML private Label hospitalCountLabel;
    @FXML private Label doctorCountLabel;
    @FXML private Label systemHealthLabel;

    // Chart components
    @FXML private LineChart<String, Number> userRegistrationsChart;
    @FXML private BarChart<String, Number> appointmentsChart;
    @FXML private CategoryAxis userChartXAxis;
    @FXML private NumberAxis userChartYAxis;
    @FXML private CategoryAxis appointmentChartXAxis;
    @FXML private NumberAxis appointmentChartYAxis;

    @FXML private LineChart<String, Number> doctorsOverTimeChart;
    @FXML private CategoryAxis doctorChartXAxis;
    @FXML private NumberAxis doctorChartYAxis;
    @FXML private PieChart userDistributionPieChart;

    private UserData userData;

    public void setUser(UserData userData) {
        this.userData = userData;
        userNameLabel.setText(userData.fullName());
        userEmailLabel.setText(userData.email());
        loadData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeCharts();
        loadData();
    }

    private void initializeCharts() {
        userRegistrationsChart.setTitle("User Registrations Over Time");
        userRegistrationsChart.setLegendVisible(false);
        appointmentsChart.setTitle("Appointments Over Time");
        appointmentsChart.setLegendVisible(false);
        doctorsOverTimeChart.setTitle("Doctors Over Time");
        doctorsOverTimeChart.setLegendVisible(false);
        userDistributionPieChart.setTitle("User Distribution");
        userDistributionPieChart.setLegendVisible(true);
    }

    private void loadData() {
        loadDashboardStats();
        loadUserRegistrationsChart();
        loadAppointmentsChart();
        loadDoctorsOverTimeChart();
        loadUserDistributionPieChart();
    }

    private void loadDashboardStats() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            // Total users
            ResultSet rsUsers = db.queryData("SELECT COUNT(*) FROM users");
            userCountLabel.setText(rsUsers.next() ? String.valueOf(rsUsers.getInt(1)) : "0");
            // Hospitals
            ResultSet rsHospitals = db.queryData("SELECT COUNT(*) FROM users WHERE role='HOSPITAL'");
            hospitalCountLabel.setText(rsHospitals.next() ? String.valueOf(rsHospitals.getInt(1)) : "0");
            // Doctors
            ResultSet rsDoctors = db.queryData("SELECT COUNT(*) FROM users WHERE role='DOCTOR'");
            doctorCountLabel.setText(rsDoctors.next() ? String.valueOf(rsDoctors.getInt(1)) : "0");
            // System Health (static for now)
            systemHealthLabel.setText("Good");
        } catch (Exception e) {
            userCountLabel.setText("0");
            hospitalCountLabel.setText("0");
            doctorCountLabel.setText("0");
            systemHealthLabel.setText("Error");
        }
    }

    private void loadUserRegistrationsChart() {
        userRegistrationsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("New Users");
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT DATE(created_at) as date, COUNT(*) as count FROM users WHERE created_at >= CURDATE() - INTERVAL 30 DAY GROUP BY DATE(created_at) ORDER BY date";
            ResultSet rs = db.queryData(sql);
            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("count");
                series.getData().add(new XYChart.Data<>(date, count));
            }
        } catch (Exception e) {
            // fallback: no data
        }
        userRegistrationsChart.getData().add(series);
    }

    private void loadAppointmentsChart() {
        appointmentsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Appointments");
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT DATE(created_at) as date, COUNT(*) as count FROM appointments WHERE created_at >= CURDATE() - INTERVAL 30 DAY GROUP BY DATE(created_at) ORDER BY date";
            ResultSet rs = db.queryData(sql);
            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("count");
                series.getData().add(new XYChart.Data<>(date, count));
            }
        } catch (Exception e) {
            // fallback: no data
        }
        appointmentsChart.getData().add(series);
    }

    private void loadDoctorsOverTimeChart() {
        doctorsOverTimeChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doctors");
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT DATE(created_at) as date, COUNT(*) as count FROM users WHERE role='DOCTOR' AND created_at >= CURDATE() - INTERVAL 30 DAY GROUP BY DATE(created_at) ORDER BY date";
            ResultSet rs = db.queryData(sql);
            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("count");
                series.getData().add(new XYChart.Data<>(date, count));
            }
        } catch (Exception e) {
            // fallback: no data
        }
        doctorsOverTimeChart.getData().add(series);
    }

    private void loadUserDistributionPieChart() {
        userDistributionPieChart.getData().clear();
        try {
            DatabaseConnection db = new DatabaseConnection();
            String sql = "SELECT role, COUNT(*) AS count\n" +
                    "FROM users\n" +
                    "WHERE role != 'ADMIN'\n" +
                    "GROUP BY role;\n";
            ResultSet rs = db.queryData(sql);
            Map<String, Integer> roleCounts = new HashMap<>();
            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                roleCounts.put(role, count);
            }
            for (Map.Entry<String, Integer> entry : roleCounts.entrySet()) {
                String label = entry.getKey().substring(0,1) + entry.getKey().substring(1).toLowerCase() + " (" + entry.getValue() + ")";
                userDistributionPieChart.getData().add(new PieChart.Data(label, entry.getValue()));
            }
        } catch (Exception e) {
            // fallback: no data
        }
    }

    @FXML
    public void handleDashboard() {
        System.out.println("Dashboard section selected");
    }

    @FXML
    public void handleUsers() {
        System.out.println("Users section selected");
    }

    @FXML
    public void handleHospitals() {
        try {
            FXMLScene.switchScene(FXMLScene.load("hospital_list.fxml"), userNameLabel.getScene().getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading hospital list: " + e.getMessage());
        }
    }

    @FXML
    public void handleDoctors() {
        try {
            FXMLScene fxmlScene = FXMLScene.load("admin-doctorlist.fxml");
            AdminDoctorListController controller = (AdminDoctorListController) fxmlScene.getController();
            controller.setUser(userData);
            FXMLScene.switchScene(fxmlScene, userNameLabel.getScene().getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading admin doctor list: " + e.getMessage());
        }
    }

    @FXML
    public void handleReports() {
        System.out.println("Reports section selected");
    }

    @FXML
    public void handleSettings() {
        System.out.println("Settings section selected");
    }

    @FXML
    public void handleLogout() {
        try {
            // Clear login info
            LoginInfoSave.clearLoginInfo();
            // Navigate back to login page
            FXMLScene.switchScene(FXMLScene.load("auth/login.fxml"), userNameLabel.getScene().getRoot());
            CustomAlertBox.showCustomAlert("Logged out successfully!", "success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlertBox.showCustomAlert("Error during logout: " + e.getMessage(), "error");
        }
    }
}
