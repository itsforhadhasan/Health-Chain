package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateUsersTable implements Migration {
    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Create users table";
    }

    @Override
    public void up(Connection connection) throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTO_INCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            full_name TEXT NOT NULL,
            phone_number TEXT NULL,
            blood_group TEXT NULL,
            role TEXT NOT NULL,
            is_verified BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }

        // Insert default admin user if not exists
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE email='admin@healthchain.com'";
        try (Statement stmt = connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(checkAdmin)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Hash the password '12345678' with SHA-256
                String password = "12345678";
                String hashed = com.aoop.healthchain.util.Encryption.getSHA256(password);
                String insertAdmin = "INSERT INTO users (email, password_hash, full_name, role, is_verified) VALUES ('admin@healthcare.com', '" + hashed + "', 'Admin User', 'ADMIN', TRUE)";
                stmt.execute(insertAdmin);
            }
        }
    }


    @Override
    public void down(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
        }
    }
}