package com.velocitypowered.proxy.bitcrow.scheduler;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;
import com.velocitypowered.proxy.util.UtilsManager;
import com.velocitypowered.proxy.util.gradient.GradientComponentFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;

public class TablistScheduler {
    public static int stage = 0;

    public static void startUpdater(VelocityServer server) {

        server.getScheduler()
                .buildTask(VelocityVirtualPlugin.INSTANCE, () -> {

                    int online = server.getPlayerCount();
                    String rawrName = UtilsManager.MiniFontConvert("<#2f5bd6>BITCROW<#70d4fc>");

                    server.getAllPlayers().forEach(player -> {
                        String playtime = "1.2d";
                        int ping = (int) player.getPing();
                        String currentServer = player.getCurrentServer()
                                .map(s -> s.getServerInfo().getName())
                                .orElse("Unknown");

                        Component header = Component.text()
                                .append(GradientComponentFormatter.applyGradient(rawrName))
                                .appendNewline()
                                .append(Component.text("Playtime: ", NamedTextColor.GRAY))
                                .append(Component.text(playtime, TextColor.fromHexString("#4077b3")))
                                .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                                .append(Component.text("Ping: ", NamedTextColor.GRAY))
                                .append(Component.text(ping + "ms", TextColor.fromHexString("#4077b3")))
                                .appendNewline()
                                .build();

                        // --- Footer ---
                        Component footer = Component.text("");
                        if(stage == 0) {
                            footer = Component.text()
                                    .appendNewline()
                                    .append(Component.text("Server: ", NamedTextColor.GRAY))
                                    .append(Component.text(currentServer, TextColor.fromHexString("#4077b3")))
                                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text("Online: ", NamedTextColor.GRAY))
                                    .append(Component.text(String.valueOf(online), TextColor.fromHexString("#4077b3")))
                                    .appendNewline()
                                    .append(Component.text("Discord: discord.gg/example", TextColor.fromHexString("#7289da")))
                                    .build();
                            stage++;
                        } else if(stage == 1) {
                            footer = Component.text()
                                    .appendNewline()
                                    .append(Component.text("Server: ", NamedTextColor.GRAY))
                                    .append(Component.text(currentServer, TextColor.fromHexString("#4077b3")))
                                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text("Online: ", NamedTextColor.GRAY))
                                    .append(Component.text(String.valueOf(online), TextColor.fromHexString("#4077b3")))
                                    .appendNewline()
                                    .append(Component.text("Website: bitcrow.org", TextColor.fromHexString("#4077b3")))
                                    .build();
                            stage = 0;
                        }

                        player.sendPlayerListHeaderAndFooter(header, footer);
                    });

                })
                .repeat(Duration.ofSeconds(10))
                .schedule();
    }
}