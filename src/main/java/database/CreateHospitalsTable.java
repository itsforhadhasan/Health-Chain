package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateHospitalsTable implements Migration {
    @Override
    public int getVersion() {
        return 2; // This is the second migration (after users table)
    }

    @Override
    public String getDescription() {
        return "Create hospitals table";
    }

    @Override
    public void up(Connection connection) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS hospitals (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                phone VARCHAR(50),
                address VARCHAR(255),
                city VARCHAR(100),
                state VARCHAR(100),
                zip VARCHAR(20),
                website VARCHAR(255),
                license_number VARCHAR(100),
                establishment_year INT,
                bed_count INT,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                user_id INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void down(Connection connection) throws SQLException {
        String sql = "DROP TABLE IF EXISTS hospitals";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
