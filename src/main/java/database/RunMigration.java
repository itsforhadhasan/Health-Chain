package database;

import com.aoop.healthchain.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class RunMigration {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseConnection db = new DatabaseConnection();
        Connection connection = db.getConnection();
        DatabaseMigration migration = new DatabaseMigration(connection);
        migration.migrate();
    }
}
