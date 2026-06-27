package com.velocitypowered.proxy.command.bitcrow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Command für einen Server-übergreifenden TeamChat mit Toggle-Funktion.
 */
public class TeamChatCMD {

    private static final Set<UUID> disabledTeamChatPlayers = new HashSet<>();

    private TeamChatCMD() {
    }

    /**
     * Erstellt den /teamchat (oder /tc) Command.
     */
    public static BrigadierCommand command(final VelocityServer server) {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("teamchat")
                        .requires(source -> source instanceof Player && source.hasPermission("bitcrow.command.teamchat"))

                        .executes(context -> {
                            Player player = (Player) context.getSource();
                            boolean currentStatus = isTeamChatEnabled(player.getUniqueId());

                            setTeamChatEnabled(player.getUniqueId(), !currentStatus);

                            if (currentStatus) {
                                player.sendMessage(VelocityServer.getPREFIX().append(
                                        Component.text("Du hast den TeamChat deaktiviert.", NamedTextColor.RED)
                                ));
                            } else {
                                player.sendMessage(VelocityServer.getPREFIX().append(
                                        Component.text("Du hast den TeamChat aktiviert.", NamedTextColor.GREEN)
                                ));
                            }
                            return Command.SINGLE_SUCCESS;
                        })

                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    Player sender = (Player) context.getSource();
                                    String message = StringArgumentType.getString(context, "message");

                                    if (!isTeamChatEnabled(sender.getUniqueId())) {
                                        sender.sendMessage(VelocityServer.getPREFIX().append(
                                                Component.text("Du hast den TeamChat deaktiviert. Aktiviere ihn mit /teamchat, um Nachrichten zu senden/sehen.", NamedTextColor.RED)
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String currentServerName = sender.getCurrentServer()
                                            .map(srv -> srv.getServerInfo().getName())
                                            .orElse("Unbekannt");

                                    Component format = VelocityServer.getTeamChatPrefix()
                                            .append(Component.text("[" + currentServerName + "] ", NamedTextColor.GRAY))
                                            .append(Component.text(sender.getUsername(), NamedTextColor.YELLOW))
                                            .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                                            .append(Component.text(message, NamedTextColor.WHITE));

                                    for (Player target : server.getAllPlayers()) {
                                        if (target.hasPermission("bitcrow.command.teamchat") && isTeamChatEnabled(target.getUniqueId())) {
                                            target.sendMessage(format);
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .build()
        );
    }

    /**
     * Prüft, ob ein Spieler den TeamChat aktiviert hat.
     */
    public static boolean isTeamChatEnabled(UUID uuid) {
        return !disabledTeamChatPlayers.contains(uuid);

    }

    /**
     * Setzt den TeamChat Status für einen Spieler.
     */
    public static void setTeamChatEnabled(UUID uuid, boolean enabled) {
        if (enabled) {
            disabledTeamChatPlayers.remove(uuid);
        } else {
            disabledTeamChatPlayers.add(uuid);
        }
    }
}