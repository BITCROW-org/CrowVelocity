package com.velocitypowered.proxy.bitcrow.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.bitcrow.mysql.MySQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class PlaytimeManager {

    private final Map<UUID, Long> playtimeCache = new ConcurrentHashMap<>();

    private final MySQLManager mySQLManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public PlaytimeManager(MySQLManager mySQLManager) {
        this.mySQLManager = mySQLManager;
    }

    public void createTable() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS playtimes (
                        uuid VARCHAR(36) PRIMARY KEY,
                        playtime BIGINT
                    )
                 """)) {

                statement.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void loadAllPlaytimes() {
        CompletableFuture.runAsync(() -> {

            playtimeCache.clear();

            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    SELECT * FROM playtimes
                 """)) {

                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long playtime = rs.getLong("playtime");

                    playtimeCache.put(uuid, playtime);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, executor);
    }

    public void loadPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {

            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    SELECT playtime FROM playtimes WHERE uuid = ?
                 """)) {

                statement.setString(1, uuid.toString());

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    playtimeCache.put(uuid, rs.getLong("playtime"));
                } else {
                    playtimeCache.put(uuid, 0L);
                    savePlaytimeAsync(uuid, 0L);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, executor);
    }

    public void unloadPlayer(UUID uuid) {
        playtimeCache.remove(uuid);
    }

    public long getPlaytime(UUID uuid) {
        return playtimeCache.getOrDefault(uuid, 0L);
    }

    public void setPlaytime(UUID uuid, long seconds) {
        playtimeCache.put(uuid, seconds);
        savePlaytimeAsync(uuid, seconds);
    }

    public void addPlaytime(UUID uuid, long seconds) {
        long newPlaytime = getPlaytime(uuid) + seconds;

        playtimeCache.put(uuid, newPlaytime);
        savePlaytimeAsync(uuid, newPlaytime);
    }

    private void savePlaytimeAsync(UUID uuid, long playtime) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = mySQLManager.getConnection();
                 PreparedStatement statement = conn.prepareStatement("""
                    INSERT INTO playtimes (uuid, playtime)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE
                    playtime = VALUES(playtime)
                 """)) {

                statement.setString(1, uuid.toString());
                statement.setLong(2, playtime);

                statement.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }
    public String getFormattedTime(Player player) {
        long seconds = getPlaytime(player.getUniqueId());

        long days = seconds / 86400;
        seconds %= 86400;

        long hours = seconds / 3600;
        seconds %= 3600;

        long minutes = seconds / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        }

        if (hours > 0) {
            return hours + "h";
        }

        return minutes + "min";
    }
}