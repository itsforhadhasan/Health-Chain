package com.aoop.healthchain.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public abstract class Model {
    protected Connection connection;
    protected String tableName;

    public Model(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    // Generic method to execute insert queries
    protected Long executeInsert(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        }
    }

    // Generic method to execute update queries
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    // Generic method to execute select queries
    protected ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    // Generic method to find by ID
    protected Optional<ResultSet> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        ResultSet rs = executeQuery(sql, id);
        return rs.next() ? Optional.of(rs) : Optional.empty();
    }

    // Generic method to find all records
    protected ResultSet findAll() throws SQLException {
        String sql = "SELECT * FROM " + tableName;
        return executeQuery(sql);
    }

    // Generic method to delete by ID
    protected boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}