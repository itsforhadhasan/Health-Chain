package com.aoop.healthchain.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private final StringProperty date;
    private final StringProperty time;
    private final StringProperty doctor;
    private final StringProperty department;
    private final StringProperty status;

    public Appointment(int id, int patientId, int doctorId, String date, String time, String doctor, String department, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.doctor = new SimpleStringProperty(doctor);
        this.department = new SimpleStringProperty(department);
        this.status = new SimpleStringProperty(status);
    }

    // Property getters
    public StringProperty dateProperty() { return date; }
    public StringProperty timeProperty() { return time; }
    public StringProperty doctorProperty() { return doctor; }
    public StringProperty departmentProperty() { return department; }
    public StringProperty statusProperty() { return status; }

    // Value getters
    public String getDate() { return date.get(); }
    public String getTime() { return time.get(); }
    public String getDoctor() { return doctor.get(); }
    public String getDepartment() { return department.get(); }
    public String getStatus() { return status.get(); }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }

    // Value setters
    public void setDate(String date) { this.date.set(date); }
    public void setTime(String time) { this.time.set(time); }
    public void setDoctor(String doctor) { this.doctor.set(doctor); }
    public void setDepartment(String department) { this.department.set(department); }
    public void setStatus(String status) { this.status.set(status); }
}
