package com.velocitypowered.proxy.bitcrow.scheduler;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.bitcrow.player.PlaytimeManager;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;

import java.time.Duration;

public class PlaytimeScheduler {
    public static void startUpdater(VelocityServer server, PlaytimeManager manager) {
        server.getScheduler()
                .buildTask(VelocityVirtualPlugin.INSTANCE, () -> {
                    server.getAllPlayers().forEach(player -> calcPlaytime(player, manager));
                })
                .repeat(Duration.ofMinutes(1))
                .schedule();
    }
    public static void calcPlaytime(Player player, PlaytimeManager manager) {
        manager.addPlaytime(player.getUniqueId(), 60L);
    }
}
