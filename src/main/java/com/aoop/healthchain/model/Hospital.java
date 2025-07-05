package com.aoop.healthchain.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Hospital {
    private final Connection connection;

    public Hospital(Connection connection) {
        this.connection = connection;
    }

    public List<HospitalData> findAll() throws SQLException {
        String sql = """
            SELECT h.*, u.email as user_email, u.role as user_role 
            FROM hospitals h 
            LEFT JOIN users u ON h.user_id = u.id
            ORDER BY h.name ASC
        """;

        List<HospitalData> hospitals = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hospitals.add(mapResultSetToHospitalData(rs));
            }
        }

        return hospitals;
    }

    public Optional<HospitalData> findById(int id) throws SQLException {
        String sql = """
            SELECT h.*, u.email as user_email, u.role as user_role 
            FROM hospitals h 
            LEFT JOIN users u ON h.user_id = u.id
            WHERE h.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToHospitalData(rs));
            }
        }

        return Optional.empty();
    }

    public Optional<HospitalData> findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT h.*, u.email as user_email, u.role as user_role 
            FROM hospitals h 
            LEFT JOIN users u ON h.user_id = u.id
            WHERE h.user_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToHospitalData(rs));
            }
        }

        return Optional.empty();
    }

    public int create(HospitalData hospital, int userId) throws SQLException {
        String sql = """
            INSERT INTO hospitals (
                name, email, phone, address, city, state, zip, 
                website, license_number, establishment_year, bed_count, status, user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, hospital.name());
            stmt.setString(2, hospital.email());
            stmt.setString(3, hospital.phone());
            stmt.setString(4, hospital.address());
            stmt.setString(5, hospital.city());
            stmt.setString(6, hospital.state());
            stmt.setString(7, hospital.zip());
            stmt.setString(8, hospital.website());
            stmt.setString(9, hospital.licenseNumber());
            stmt.setInt(10, hospital.establishmentYear());
            stmt.setInt(11, hospital.bedCount());
            stmt.setString(12, hospital.status());
            stmt.setInt(13, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }

        return -1;
    }

    public boolean update(HospitalData hospital) throws SQLException {
        String sql = """
            UPDATE hospitals SET 
                name = ?, email = ?, phone = ?, address = ?, city = ?, state = ?, 
                zip = ?, website = ?, license_number = ?, establishment_year = ?, 
                bed_count = ?, status = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, hospital.name());
            stmt.setString(2, hospital.email());
            stmt.setString(3, hospital.phone());
            stmt.setString(4, hospital.address());
            stmt.setString(5, hospital.city());
            stmt.setString(6, hospital.state());
            stmt.setString(7, hospital.zip());
            stmt.setString(8, hospital.website());
            stmt.setString(9, hospital.licenseNumber());
            stmt.setInt(10, hospital.establishmentYear());
            stmt.setInt(11, hospital.bedCount());
            stmt.setString(12, hospital.status());
            stmt.setInt(13, hospital.id());

            return stmt.executeUpdate() > 0;
        }
    }

    private HospitalData mapResultSetToHospitalData(ResultSet rs) throws SQLException {
        return new HospitalData(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("zip"),
            rs.getString("website"),
            rs.getString("license_number"),
            rs.getInt("establishment_year"),
            rs.getInt("bed_count"),
            rs.getString("status"),
            rs.getInt("user_id"),
            rs.getString("user_email"),
            rs.getString("user_role"),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
}
