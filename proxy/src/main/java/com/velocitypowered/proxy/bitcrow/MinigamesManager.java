package com.velocitypowered.proxy.bitcrow;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.proxy.util.AddressUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MinigamesManager {

    private final List<MinigamesObject> minigamesObjects = new ArrayList<>();
    private final ProxyServer proxy;

    public MinigamesManager(ProxyServer proxy) {
        this.proxy = proxy;
    }

    public void register(MinigamesObject object) {
        if (object == null) return;
        if (getByName(object.getName()) != null) return;

        ServerInfo info = new ServerInfo(
                object.getName(),
                AddressUtil.parseAddress(object.getIp() + ":" + object.getPort())
        );

        object.setServerInfo(info);

        boolean online = isServerOnline(info);

        if (online) {
            if (object.getState() == null) {
                object.setState(MinigamesState.WAITING);
            }
        } else {
            object.setState(MinigamesState.OFFLINE);
        }

        minigamesObjects.add(object);
        proxy.registerServer(info);
    }

    public void unregister(String name) {
        MinigamesObject obj = getByName(name);
        if (obj == null) return;

        obj.setState(MinigamesState.OFFLINE);

        proxy.unregisterServer(obj.getServerInfo());
        minigamesObjects.remove(obj);
    }

    public MinigamesObject getByName(String name) {
        for (MinigamesObject obj : minigamesObjects) {
            if (obj.getName().equalsIgnoreCase(name)) {
                refreshConnectionState(obj);
                return obj;
            }
        }
        return null;
    }

    public List<MinigamesObject> getAll() {
        for (MinigamesObject obj : minigamesObjects) {
            refreshConnectionState(obj);
        }
        return minigamesObjects;
    }

    private void refreshConnectionState(MinigamesObject obj) {
        boolean online = isServerOnline(obj.getServerInfo());

        if (!online) {
            obj.setState(MinigamesState.OFFLINE);
        } else {
            if (obj.getState() == MinigamesState.OFFLINE) {
                obj.setState(MinigamesState.WAITING);
            }
        }
    }

    private boolean isServerOnline(ServerInfo info) {
        try {
            return proxy.getServer(info.getName())
                    .map(server -> {
                        try {
                            return server.ping()
                                    .handle((ping, throwable) -> throwable == null)
                                    .join();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    public List<Player> getPlayers(String serverName) {
        List<Player> result = new ArrayList<>();

        for (Player player : proxy.getAllPlayers()) {
            player.getCurrentServer().ifPresent(server -> {
                if (server.getServerInfo().getName().equalsIgnoreCase(serverName)) {
                    result.add(player);
                }
            });
        }

        return result;
    }

    public int getPlayerCount(String serverName) {
        AtomicInteger count = new AtomicInteger();

        for (Player player : proxy.getAllPlayers()) {
            player.getCurrentServer().ifPresent(server -> {
                if (server.getServerInfo().getName().equalsIgnoreCase(serverName)) {
                    count.incrementAndGet();
                }
            });
        }

        return count.get();
    }

    public List<MinigamesObject> getAvailableServers() {
        List<MinigamesObject> result = new ArrayList<>();

        for (MinigamesObject obj : minigamesObjects) {
            if (obj.canJoin()) {
                result.add(obj);
            }
        }

        return result;
    }

    public int size() {
        return minigamesObjects.size();
    }
}