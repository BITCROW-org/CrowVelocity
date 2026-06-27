package com.velocitypowered.proxy.bitcrow;

import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class MinigamesObject {

    @Getter
    @Setter
    private MinigamesState state;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String ip;

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private int maxPlayers;

    @Getter
    @Setter
    private int minPlayers;

    @Getter
    @Setter
    private long createdAt;

    @Getter
    @Setter
    private ServerInfo serverInfo;

    public int getCurrentPlayerCount() {
        return 0;
    }

    public boolean isFull() {
        return getCurrentPlayerCount() >= maxPlayers;
    }

    public boolean canJoin() {
        return state == MinigamesState.WAITING && !isFull();
    }
}