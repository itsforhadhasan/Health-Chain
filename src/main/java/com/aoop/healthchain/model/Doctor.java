package com.aoop.healthchain.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private final Connection connection;

    public Doctor(Connection connection) {
        this.connection = connection;
    }

    public List<DoctorData> findAll() throws SQLException {
        String sql = """
            SELECT d.*, u.full_name as name, u.email as email, h.name as hospital_name
            FROM doctors d
            JOIN users u ON d.user_id = u.id
            LEFT JOIN hospitals h ON d.hospital_id = h.id
            ORDER BY u.full_name ASC
        """;

        List<DoctorData> doctors = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                doctors.add(mapResultSetToDoctorData(rs));
            }
        }

        return doctors;
    }

    public List<DoctorData> findByHospitalId(int hospitalId) throws SQLException {
        String sql = """
            SELECT d.*, u.full_name as name, u.email as email, h.name as hospital_name
            FROM doctors d
            JOIN users u ON d.user_id = u.id
            LEFT JOIN hospitals h ON d.hospital_id = h.id
            WHERE d.hospital_id = ?
            ORDER BY u.full_name ASC
        """;

        List<DoctorData> doctors = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, hospitalId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                doctors.add(mapResultSetToDoctorData(rs));
            }
        }

        return doctors;
    }

    public boolean create(int userId, String specialization, String phone, String address, String city, String state, String zip, String licenseNumber, String issueDate, String expiryDate, int hospitalId) throws SQLException {
        String sql = """
            INSERT INTO doctors (user_id, specialization, phone, address, city, state, zip, license_number, issue_date, expiry_date, hospital_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, specialization);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, city);
            stmt.setString(6, state);
            stmt.setString(7, zip);
            stmt.setString(8, licenseNumber);
            stmt.setString(9, issueDate);
            stmt.setString(10, expiryDate);
            stmt.setInt(11, hospitalId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean update(int doctorId, String specialization, String phone, String address, String city, String state, String zip, String licenseNumber, String issueDate, String expiryDate, String status) throws SQLException {
        String sql = """
            UPDATE doctors 
            SET specialization = ?, phone = ?, address = ?, city = ?, state = ?, zip = ?, 
                license_number = ?, issue_date = ?, expiry_date = ?, status = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, specialization);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, city);
            stmt.setString(5, state);
            stmt.setString(6, zip);
            stmt.setString(7, licenseNumber);
            stmt.setString(8, issueDate);
            stmt.setString(9, expiryDate);
            stmt.setString(10, status);
            stmt.setInt(11, doctorId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            return stmt.executeUpdate() > 0;
        }
    }

    public DoctorData findById(int doctorId) throws SQLException {
        String sql = """
            SELECT d.*, u.full_name as name, u.email as email, h.name as hospital_name
            FROM doctors d
            JOIN users u ON d.user_id = u.id
            LEFT JOIN hospitals h ON d.hospital_id = h.id
            WHERE d.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToDoctorData(rs);
            }
        }

        return null;
    }

    private DoctorData mapResultSetToDoctorData(ResultSet rs) throws SQLException {
        return new DoctorData(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("specialization"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("zip"),
            rs.getString("license_number"),
            rs.getString("issue_date"),
            rs.getString("expiry_date"),
            rs.getString("status"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("hospital_name")
        );
    }

    // findAll, findById, etc. methods will be added here
} 