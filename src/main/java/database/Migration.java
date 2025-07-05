package database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
    int getVersion();
    String getDescription();
    void up(Connection connection) throws SQLException;
    void down(Connection connection) throws SQLException;
}