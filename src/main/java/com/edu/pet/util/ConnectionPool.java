package com.edu.pet.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionPool {

    private static final Properties PROPERTIES;
    private static final HikariConfig CONFIG;
    private static final HikariDataSource DATA_SOURCE;

    private ConnectionPool() {

    }

    static {
        PROPERTIES = new Properties();
        loadProperties();
        CONFIG = new HikariConfig(PROPERTIES);
        DATA_SOURCE = new HikariDataSource(CONFIG);
    }

    private static void loadProperties() {
        try (InputStream inputStream = ConnectionPool.class.getClassLoader().getResourceAsStream("hikari.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
