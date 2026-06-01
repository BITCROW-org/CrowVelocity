package com.velocitypowered.proxy.bitcrow.player;

import com.velocitypowered.proxy.bitcrow.mysql.MySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.*;

public class PlayerManager {

    private final Map<UUID, PlayerData> playersByUUID = new ConcurrentHashMap<>();
    private final Map<String, PlayerData> playersByName = new ConcurrentHashMap<>();

    private final MySQLManager mySQLManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public PlayerManager(MySQLManager mySQLManager) {
        this.mySQLManager = mySQLManager;
    }

    public void createTable() {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        username VARCHAR(16),
                        current_server VARCHAR(100)
                    )
                 """)) {

                statement.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void addPlayer(UUID uuid, String username, String server) {
        PlayerData data = new PlayerData(uuid, username, server);

        playersByUUID.put(uuid, data);
        playersByName.put(username.toLowerCase(), data);

        savePlayerAsync(data);
    }

    public void removePlayer(UUID uuid) {
        PlayerData data = playersByUUID.remove(uuid);

        if (data != null) {
            playersByName.remove(data.getUsername().toLowerCase());
            deletePlayerAsync(uuid);
        }
    }

    public void updateServer(UUID uuid, String server) {
        PlayerData data = playersByUUID.get(uuid);

        if (data == null) return;

        data.setCurrentServer(server);
        updateServerAsync(uuid, server);
    }

    public PlayerData getPlayer(UUID uuid) {
        return playersByUUID.get(uuid);
    }

    public PlayerData getPlayer(String username) {
        return playersByName.get(username.toLowerCase());
    }

    public List<PlayerData> getPlayersAlphabetically() {
        List<PlayerData> list = new ArrayList<>(playersByUUID.values());
        list.sort(Comparator.comparing(PlayerData::getUsername));
        return list;
    }

    private void savePlayerAsync(PlayerData data) {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    INSERT INTO players (uuid, username, current_server)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                    username = VALUES(username),
                    current_server = VALUES(current_server)
                 """)) {

                statement.setString(1, data.getUuid().toString());
                statement.setString(2, data.getUsername());
                statement.setString(3, data.getCurrentServer());

                statement.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    private void updateServerAsync(UUID uuid, String server) {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    UPDATE players
                    SET current_server = ?
                    WHERE uuid = ?
                 """)) {

                statement.setString(1, server);
                statement.setString(2, uuid.toString());

                statement.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    private void deletePlayerAsync(UUID uuid) {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    DELETE FROM players WHERE uuid = ?
                 """)) {

                statement.setString(1, uuid.toString());
                statement.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void clearAllPlayersFromDB() {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    DELETE FROM players
                 """)) {

                statement.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }
    public void reloadPlayersFromDatabase() {
        if (!mySQLManager.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {

            Map<UUID, PlayerData> uuidMap = new ConcurrentHashMap<>();
            Map<String, PlayerData> nameMap = new ConcurrentHashMap<>();

            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                SELECT * FROM players
             """)) {

                ResultSet rs = statement.executeQuery();

                while (rs.next()) {

                    PlayerData data = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getString("current_server")
                    );

                    uuidMap.put(data.getUuid(), data);
                    nameMap.put(data.getUsername().toLowerCase(), data);
                }

                playersByUUID.clear();
                playersByUUID.putAll(uuidMap);

                playersByName.clear();
                playersByName.putAll(nameMap);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, executor);
    }
}
