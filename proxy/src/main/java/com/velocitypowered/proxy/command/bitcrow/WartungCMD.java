package com.velocitypowered.proxy.command.bitcrow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WartungCMD {

    private WartungCMD() {
    }

    public static BrigadierCommand command(final VelocityServer server) {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("wartung")
                        .requires(source -> source.hasPermission("bitcrow.wartung"))
                        .executes(context -> {
                            CommandSource sender = context.getSource();

                            sender.sendMessage(
                                    VelocityServer.getPREFIX().append(Component.text("Nutze: on, off, status", NamedTextColor.GRAY))
                            );

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                                .<CommandSource, String>argument("mode", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("on");
                                    builder.suggest("off");
                                    builder.suggest("status");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSource sender = context.getSource();
                                    String mode = StringArgumentType.getString(context, "mode");

                                    switch (mode.toLowerCase()) {

                                        case "on" -> {
                                            server.getConfigCfg().set("wartung", true);

                                            sender.sendMessage(
                                                    VelocityServer.getPREFIX().append(
                                                            Component.text("Wartungsmodus ", NamedTextColor.GRAY)
                                                                    .decoration(TextDecoration.BOLD, false)
                                                                    .append(Component.text("aktiviert", NamedTextColor.GREEN))
                                                    )
                                            );

                                            for (Player player : server.getAllPlayers()) {
                                                if (!player.hasPermission("bitcrow.wartung.join")) {
                                                    player.disconnect(
                                                            VelocityServer.getPREFIX2()
                                                                    .appendNewline()
                                                                    .append(Component.text("Wartungsmodus aktiv", NamedTextColor.RED))
                                                                    .appendNewline()
                                                                    .append(Component.text("Wir sind gleich wieder da", NamedTextColor.GRAY))
                                                    );
                                                }
                                            }
                                        }

                                        case "off" -> {
                                            server.getConfigCfg().set("wartung", false);

                                            sender.sendMessage(
                                                    VelocityServer.getPREFIX().append(
                                                            Component.text("Wartungsmodus deaktiviert", NamedTextColor.RED)
                                                    )
                                            );
                                        }

                                        case "status" -> {
                                            boolean status = server.getConfigCfg().getBoolean("wartung", false);

                                            sender.sendMessage(
                                                    VelocityServer.getPREFIX().append(
                                                            Component.text("Wartung ist aktuell ", NamedTextColor.GRAY)
                                                                    .append(Component.text(
                                                                            status ? "AKTIV" : "INAKTIV",
                                                                            status ? NamedTextColor.GREEN : NamedTextColor.RED
                                                                    ))
                                                    )
                                            );
                                        }

                                        default -> sender.sendMessage(
                                                VelocityServer.getPREFIX().append(
                                                        Component.text("Ungültig: on, off, status", NamedTextColor.RED)
                                                )
                                        );
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .build()
        );
    }
    public static boolean isMaintenanceMode(VelocityServer server) {
        return server.getConfigCfg().getBoolean("wartung", false);
    }
}