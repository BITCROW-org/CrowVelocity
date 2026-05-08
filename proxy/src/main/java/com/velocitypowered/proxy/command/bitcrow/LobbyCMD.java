package com.velocitypowered.proxy.command.bitcrow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.VelocityServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

/**
 * Command that connects a player to the configured lobby server.
 */
public class LobbyCMD {

    private LobbyCMD() {
    }

    /**
     * Creates the /lobby command.
     *
     * <p>Attempts to connect the player to the lobby server defined in the configuration.
     * If the server is not registered or unavailable, the player receives an error message.</p>
     *
     * @param server the Velocity proxy instance
     * @return the built BrigadierCommand
     */
    public static BrigadierCommand command(final VelocityServer server) {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("lobby")
                        .requires(source -> source instanceof Player)
                        .executes(context -> {
                            Player player = (Player) context.getSource();

                            String lobbyName = server.getLobbyCfg()
                                    .getString("lobby.server", "default");

                            Optional<RegisteredServer> target = server.getServer(lobbyName);

                            if (target.isEmpty()) {
                                player.sendMessage(server.getPREFIX().append(
                                        Component.text("Lobby-Server nicht verfügbar.", NamedTextColor.RED)
                                ));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (player.getCurrentServer()
                                    .map(info -> info.getServer().getServerInfo().getName().equalsIgnoreCase(lobbyName))
                                    .orElse(false)) {

                                player.sendMessage(server.getPREFIX().append(
                                        Component.text("Du bist bereits in der Lobby.", NamedTextColor.YELLOW)
                                ));
                                return Command.SINGLE_SUCCESS;
                            }

                            player.sendMessage(server.getPREFIX().append(
                                    Component.text("Verbinde zur Lobby...", NamedTextColor.GREEN)
                            ));

                            target.get().ping().whenComplete((result, error) -> {
                                if (error != null) {
                                    player.sendMessage(server.getPREFIX().append(
                                            Component.text("Verbindung fehlgeschlagen.", NamedTextColor.RED)
                                    ));
                                    return;
                                }

                                player.createConnectionRequest(target.get())
                                        .connectWithIndication()
                                        .thenAccept(success -> {
                                            if (!success) {
                                                player.sendMessage(server.getPREFIX().append(
                                                        Component.text("Lobby nicht erreichbar.", NamedTextColor.RED)
                                                ));
                                            }
                                        });
                            });

                            return Command.SINGLE_SUCCESS;
                        })
                        .build()
        );
    }
}