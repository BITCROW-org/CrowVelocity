package com.velocitypowered.proxy.bitcrow.player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String username;
    private String currentServer;

    public PlayerData(UUID uuid, String username, String currentServer) {
        this.uuid = uuid;
        this.username = username;
        this.currentServer = currentServer;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(String currentServer) {
        this.currentServer = currentServer;
    }
}