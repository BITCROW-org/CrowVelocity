package com.velocitypowered.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.bitcrow.MinigamesManager;
import com.velocitypowered.proxy.bitcrow.MinigamesObject;
import com.velocitypowered.proxy.bitcrow.MinigamesState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MinigamesCMD {

    private static final Component SYNTAX = Component.text(
            "Usage: /minigames <list|register|unregister|status>",
            NamedTextColor.RED
    );

    public static BrigadierCommand create(final MinigamesManager manager) {

        final LiteralCommandNode<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder("minigames")
                .requires(src -> !(src instanceof Player))
                .executes(ctx -> {
                    ctx.getSource().sendMessage(SYNTAX);
                    return Command.SINGLE_SUCCESS;
                })

                .then(BrigadierCommand.literalArgumentBuilder("list")
                        .executes(ctx -> {

                            CommandSource source = ctx.getSource();

                            if (manager.getAll().isEmpty()) {
                                source.sendMessage(Component.text("no servers registered", NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            for (MinigamesObject obj : manager.getAll()) {
                                source.sendMessage(Component.text(
                                        obj.getName()
                                                + " | players: " + manager.getPlayerCount(obj.getName())
                                                + " | state: " + obj.getState(),
                                        NamedTextColor.YELLOW
                                ));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )

                .then(BrigadierCommand.literalArgumentBuilder("register")
                        .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                                .then(BrigadierCommand.requiredArgumentBuilder("ip", StringArgumentType.word())
                                        .then(BrigadierCommand.requiredArgumentBuilder("port", IntegerArgumentType.integer())
                                                .executes(ctx -> {

                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String ip = StringArgumentType.getString(ctx, "ip");
                                                    int port = IntegerArgumentType.getInteger(ctx, "port");

                                                    MinigamesObject obj = new MinigamesObject();
                                                    obj.setName(name);
                                                    obj.setIp(ip);
                                                    obj.setPort(port);
                                                    obj.setState(MinigamesState.WAITING);

                                                    manager.register(obj);

                                                    ctx.getSource().sendMessage(Component.text(
                                                            "registered " + name,
                                                            NamedTextColor.GREEN
                                                    ));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(Component.text(
                                    "Usage: /minigames register <name> <ip> <port>",
                                    NamedTextColor.RED
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                )

                .then(BrigadierCommand.literalArgumentBuilder("unregister")
                        .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                                .executes(ctx -> {

                                    String name = StringArgumentType.getString(ctx, "name");

                                    manager.unregister(name);

                                    ctx.getSource().sendMessage(Component.text(
                                            "unregistered " + name,
                                            NamedTextColor.RED
                                    ));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(Component.text(
                                    "Usage: /minigames unregister <name>",
                                    NamedTextColor.RED
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                )

                .then(BrigadierCommand.literalArgumentBuilder("status")
                        .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                                .executes(ctx -> {

                                    String name = StringArgumentType.getString(ctx, "name");

                                    MinigamesObject obj = manager.getByName(name);

                                    if (obj == null) {
                                        ctx.getSource().sendMessage(Component.text(
                                                "not found",
                                                NamedTextColor.RED
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    ctx.getSource().sendMessage(Component.text(
                                            obj.getName()
                                                    + " | ip: " + obj.getIp() + ":" + obj.getPort()
                                                    + " | players: " + manager.getPlayerCount(obj.getName())
                                                    + " | state: " + obj.getState(),
                                            NamedTextColor.AQUA
                                    ));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(Component.text(
                                    "Usage: /minigames status <name>",
                                    NamedTextColor.RED
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                )

                .build();

        return new BrigadierCommand(node);
    }
}