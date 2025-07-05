package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDoctorsTable implements Migration {
    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getDescription() {
        return "Create doctors table";
    }

    @Override
    public void up(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS doctors (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        hospital_id INT NOT NULL DEFAULT 1,
                        specialization VARCHAR(255),
                        phone VARCHAR(255) UNIQUE,
                        address VARCHAR(255),
                        city VARCHAR(255),
                        state VARCHAR(255),
                        zip VARCHAR(50),
                        license_number VARCHAR(255) UNIQUE,
                        issue_date DATE,
                        expiry_date DATE,
                        status VARCHAR(50) DEFAULT 'PENDING',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE
                    );
                    """;
            statement.executeUpdate(sql);
            System.out.println("Migration 'CreateDoctorsTable' executed successfully.");
        }
    }

    @Override
    public void down(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "DROP TABLE IF EXISTS doctors;";
            statement.executeUpdate(sql);
            System.out.println("Migration 'CreateDoctorsTable' reverted successfully.");
        }
    }
} 