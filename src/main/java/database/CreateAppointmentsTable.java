package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateAppointmentsTable implements Migration {
    @Override
    public int getVersion() {
        return 5; // Next migration version
    }

    @Override
    public String getDescription() {
        return "Create appointments table";
    }

    @Override
    public void up(Connection connection) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id INT NOT NULL,
                doctor_id INT NOT NULL,
                date DATE NOT NULL,
                time VARCHAR(20) NOT NULL,
                department VARCHAR(255),
                status VARCHAR(50) DEFAULT 'upcoming',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void down(Connection connection) throws SQLException {
        String sql = "DROP TABLE IF EXISTS appointments;";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
} 