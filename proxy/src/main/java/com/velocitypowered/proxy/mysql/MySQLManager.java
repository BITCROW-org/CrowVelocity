package com.velocitypowered.proxy.mysql;

import com.velocitypowered.proxy.VelocityServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public class MySQLManager {

    private HikariDataSource dataSource;

    public boolean connect(String host, int port, String database, String user, String password) {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            config.setUsername(user);
            config.setPassword(password);

            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(5000);
            config.setInitializationFailTimeout(-1);

            this.dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(2)) {
                    System.out.println("[MySQL] Login successful!");
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("[MySQL] connection failed:" + e.getMessage());
            this.dataSource = null;
            return false;
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}