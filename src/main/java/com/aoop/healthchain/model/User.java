package com.aoop.healthchain.model;


import com.aoop.healthchain.util.Encryption;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class User extends Model {
    public User(Connection connection) {
        super(connection, "users");
    }

    public Long create(String email, String passwordHash, String fullName, 
                      String phoneNumber, String bloodGroup, String role) throws SQLException {
        String sql = """
            INSERT INTO users (email, password_hash, full_name, phone_number, blood_group, role)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        return executeInsert(sql, email, Encryption.getSHA256(passwordHash), fullName, phoneNumber, bloodGroup, role);
    }

    public int create(String fullName, String email,String phone, String password, Role role) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone_number, password_hash, role, is_verified) VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, Encryption.getSHA256(password));
            statement.setString(5, role.toString());
            statement.setBoolean(6, true); // Doctors are verified by default for now
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public Optional<UserData> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        ResultSet rs = executeQuery(sql, email);
        
        if (rs.next()) {
            UserData userData = new UserData(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getString("blood_group"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getBoolean("is_verified")
            );
            return Optional.of(userData);
        }
        return Optional.empty();
    }

    public boolean updateUser(Long id, String fullName, String phoneNumber, 
                            String bloodGroup) throws SQLException {
        String sql = """
            UPDATE users 
            SET full_name = ?, phone_number = ?, blood_group = ?
            WHERE id = ?
        """;
        return executeUpdate(sql, fullName, phoneNumber, bloodGroup, id) > 0;
    }

    public List<UserData> getAllUsers() throws SQLException {
        ResultSet rs = findAll();
        List<UserData> users = new ArrayList<>();
        
        while (rs.next()) {
            users.add(new UserData(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getString("blood_group"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getBoolean("is_verified")
            ));
        }
        return users;
    }

    public void updatePassword(Long id, String newPasswordText) {
        String sql = """
            UPDATE users 
            SET password_hash = ?
            WHERE id = ?
        """;
        try {
            executeUpdate(sql, newPasswordText, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVerificationStatus(Long id, boolean b) {
        String sql = """
            UPDATE users 
            SET is_verified = ?
            WHERE id = ?
        """;
        try {
            executeUpdate(sql, b, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

