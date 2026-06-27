package com.velocitypowered.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

public final class ServerListConsoleCommand {

    public static BrigadierCommand create(final ProxyServer server) {
        final LiteralCommandNode<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder("servers")
                .requires(src -> !(src instanceof com.velocitypowered.api.proxy.Player))
                .executes(ctx -> {
                    final CommandSource source = ctx.getSource();

                    for (final RegisteredServer s : server.getAllServers()) {
                        final String name = s.getServerInfo().getName();
                        final String ip = s.getServerInfo().getAddress().getHostString() + ":" + s.getServerInfo().getAddress().getPort();
                        source.sendMessage(Component.text(name + " -> " + ip));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(node);
    }
}