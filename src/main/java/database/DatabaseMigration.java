package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseMigration {
    private static final Logger logger = Logger.getLogger(DatabaseMigration.class.getName());
    private static final List<Migration> MIGRATIONS = Arrays.asList(
            new CreateUsersTable(),
            new CreateHospitalsTable(),
            new CreateDoctorsTable(),
            new CreateAppointmentsTable()
    );

    private final Connection connection;

    public DatabaseMigration(Connection connection) {
        this.connection = connection;
    }

    public void migrate() {
        try {
            createMigrationTable();

            for (Migration migration : MIGRATIONS) {
                if (!isMigrationApplied(migration.getVersion())) {
                    logger.info("Applying migration: " + migration.getDescription());
                    migration.up(connection);
                    recordMigration(migration);
                }
            }
            logger.info("Database migration check completed.");
        } catch (SQLException e) {
            logger.severe("Migration failed: " + e.getMessage());
            throw new RuntimeException("Migration failed", e);
        }
    }

    private void createMigrationTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS migrations (
                version INT PRIMARY KEY,
                description TEXT NOT NULL,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private boolean isMigrationApplied(int version) throws SQLException {
        var sql = "SELECT COUNT(*) FROM migrations WHERE version = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, version);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void recordMigration(Migration migration) throws SQLException {
        var sql = "INSERT INTO migrations (version, description) VALUES (?, ?)";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, migration.getVersion());
            stmt.setString(2, migration.getDescription());
            stmt.executeUpdate();
        }
    }
}

