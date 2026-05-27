package com.velocitypowered.proxy.command.bitcrow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class FindCMD {

    private FindCMD() {}

    public static BrigadierCommand command(final VelocityServer server) {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("find")
                        .requires(source ->
                                source.getPermissionValue("bitcrow.command.find") == Tristate.TRUE)
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                                .<CommandSource, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();

                                    server.getPlayerManager().getPlayersAlphabetically().forEach(data -> {
                                        String name = data.getUsername();

                                        if (name != null && name.toLowerCase().startsWith(input)) {
                                            builder.suggest(name);
                                        }
                                    });

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    String name = StringArgumentType.getString(context, "player");

                                    Optional<Player> target = server.getPlayer(name);

                                    if (target.isEmpty()) {
                                        source.sendMessage(
                                                server.getPREFIX().append(
                                                        Component.text("Player not found.", NamedTextColor.RED)
                                                )
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Player player = target.get();

                                    String serverName = server.getPlayerManager().getPlayer(player.getUniqueId()).getCurrentServer();

                                    source.sendMessage(
                                            server.getPREFIX().append(
                                                    Component.text(
                                                            player.getUsername() + " is currently on " + serverName,
                                                            NamedTextColor.YELLOW
                                                    )
                                            )
                                    );

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .build()
        );
    }
}