package com.aoop.healthchain.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.aoop.healthchain.util.DatabaseConnection;
// com.aoop.healthchain.model.Example usage
public class Example {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseConnection db = new DatabaseConnection();
        try (Connection connection = db.getConnection()) {
            User userModel = new User(connection);

            // Create a new user
            Long userId = userModel.create(
                "john@example.com",
                "hashedPassword123",
                "John Doe",
                "+1234567890",
                "A+",
                "PATIENT"
            );

            // Find user by email
            Optional<UserData> user = userModel.findByEmail("john@example.com");
            user.ifPresent(userData -> {
                System.out.println("Found user: " + userData.fullName());
            });

            // Update user information
            userModel.updateUser(userId, "John Smith", "+1987654321", "B+");

            // Get all users
            List<UserData> allUsers = userModel.getAllUsers();
            allUsers.forEach(userData -> {
                System.out.println("User: " + userData.fullName());
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}