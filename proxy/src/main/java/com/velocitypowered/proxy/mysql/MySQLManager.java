package com.velocitypowered.proxy.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLManager {

    private HikariDataSource dataSource;

    public boolean connect(String host, int port, String database, String user, String password) {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(3000);
            config.setInitializationFailTimeout(-1);

            this.dataSource = new HikariDataSource(config);

            System.out.println("[MySQL] Connected!");
            return true;

        } catch (Exception e) {
            System.err.println("[MySQL] WARNING: connection failed, running without DB");
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