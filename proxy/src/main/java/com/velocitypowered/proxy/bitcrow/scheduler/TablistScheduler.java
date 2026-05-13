package com.velocitypowered.proxy.bitcrow.scheduler;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;
import com.velocitypowered.proxy.util.UtilsManager;
import com.velocitypowered.proxy.util.gradient.GradientComponentFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.time.Duration;

public class TablistScheduler {
    public static int stage = 0;

    public static void startUpdater(VelocityServer server) {
        server.getScheduler()
                .buildTask(VelocityVirtualPlugin.INSTANCE, () -> server.getAllPlayers().forEach(player -> updatePlayer(server, player)))
                .repeat(Duration.ofMillis(100))
                .schedule();
    }

    public static void updatePlayer(VelocityServer server, Player player) {
        int online = server.getPlayerCount();
        String rawrName = UtilsManager.MiniFontConvert("<#2f5bd6>BITCROW<#70d4fc>");

        String playtime = server.getPlaytimeManager().getFormattedTime(player);
        int ping = (int) player.getPing();
        String currentServer = player.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("Unknown");
        Component header = Component.text()
                .append(GradientComponentFormatter.applyGradient(rawrName))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Playtime: ", NamedTextColor.GRAY))
                .append(Component.text(playtime, TextColor.fromHexString("#4077b3")))
                .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Ping: ", NamedTextColor.GRAY))
                .append(Component.text(ping + "ms", TextColor.fromHexString("#4077b3")))
                .appendNewline()
                .build();

        Component footer;
        if (stage == 0) {
            footer = Component.text()
                    .appendNewline()
                    .append(Component.text("Server: ", NamedTextColor.GRAY))
                    .append(Component.text(UtilsManager.convertText(currentServer), TextColor.fromHexString("#4077b3")))
                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Online: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(online), TextColor.fromHexString("#4077b3")))
                    .appendNewline()
                    .append(Component.text("Discord: dc.bitcrow.org", TextColor.fromHexString("#7289da")))
                    //.append(Component.text("Website: bitcrow.org", TextColor.fromHexString("#4077b3")))
                    .build();
            stage++;

        } else if (stage == 1) {
            footer = Component.text()
                    .appendNewline()
                    .append(Component.text("Server: ", NamedTextColor.GRAY))
                    .append(Component.text(UtilsManager.convertText(currentServer), TextColor.fromHexString("#4077b3")))
                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Online: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(online), TextColor.fromHexString("#4077b3")))
                    .appendNewline()
                    .append(Component.text("Discord: dc.bitcrow.org", TextColor.fromHexString("#7289da")))
                    //.append(Component.text("Website: bitcrow.org", TextColor.fromHexString("#4077b3")))
                    .build();
            stage = 0;
        } else {
            footer = Component.text()
                    .appendNewline()
                    .append(Component.text("Server: ", NamedTextColor.GRAY))
                    .append(Component.text(UtilsManager.convertText(currentServer), TextColor.fromHexString("#4077b3")))
                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Online: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(online), TextColor.fromHexString("#4077b3")))
                    .appendNewline()
                    .append(Component.text("Discord: dc.bitcrow.org", TextColor.fromHexString("#7289da")))
                    //.append(Component.text("Website: bitcrow.org", TextColor.fromHexString("#4077b3")))
                    .build();
            stage = 0;
        }
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}