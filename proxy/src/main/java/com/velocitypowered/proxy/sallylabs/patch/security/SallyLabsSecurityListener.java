/*
 * Copyright (C) 2026 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.sallylabs.patch.security;

import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.connection.PreTransferEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.StyleContext;

/**
 * Event-level hardening for public proxy entrypoints.
 */
public final class SallyLabsSecurityListener {

    private static final Logger logger = LogManager.getLogger(SallyLabsSecurityListener.class);
    private static final long ONE_MINUTE_MILLIS = 60_000L;

    private static final Component SECURITY_DENY = Component.text()
            .append(Component.text("⛔", NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD))
            .appendNewline()
            .appendNewline()
            .append(Component.text("Deine Verbindung wurde aus Sicherheitsgründen blockiert.", NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("Bitte versuche es in einigen Minuten erneut.", NamedTextColor.GRAY))
            .appendNewline()
            .appendNewline()
            .build();

    private static final Component COMMAND_DENY = Component.empty(); //Maybe remove later tab completion

  private final VelocityServer server;
  private final WindowRateLimiter loginLimiter = new WindowRateLimiter(ONE_MINUTE_MILLIS);
  private final WindowRateLimiter pingLimiter = new WindowRateLimiter(ONE_MINUTE_MILLIS);
  private final VirtualHostSecurity virtualHostSecurity = new VirtualHostSecurity();

  public SallyLabsSecurityListener(VelocityServer server) {
    this.server = server;
  }

  @Subscribe
  public void onPreLogin(PreLoginEvent event) {
    SallyLabsPatchConfig config = server.getConfiguration().getSallyLabsPatchConfig();
    if (!config.isEnabled()) {
      return;
    }

    InetAddress address = event.getConnection().getRemoteAddress().getAddress();
    if (!loginLimiter.allow(address, config.getMaxLoginsPerMinutePerIp())) {
      logger.warn("Blocked login flood from {}", address.getHostAddress());
      event.setResult(PreLoginComponentResult.denied(SECURITY_DENY));
      return;
    }

    if (!isUsernameAllowed(event.getUsername(), config.getUsernamePattern())) {
      logger.warn("Blocked invalid username '{}' from {}", event.getUsername(),
          address.getHostAddress());
      event.setResult(PreLoginComponentResult.denied(SECURITY_DENY));
      return;
    }

    if (!isVirtualHostShapeAllowed(event, config)) {
      logger.warn("Blocked login from {} with malformed or missing virtual host {}",
          address.getHostAddress(), event.getConnection().getRawVirtualHost().orElse("<none>"));
      event.setResult(PreLoginComponentResult.denied(SECURITY_DENY));
      return;
    }

    if (!isVirtualHostAllowed(event, config)) {
      logger.warn("Blocked login from {} with virtual host {}", address.getHostAddress(),
          event.getConnection().getRawVirtualHost().orElse("<none>"));
      event.setResult(PreLoginComponentResult.denied(SECURITY_DENY));
      return;
    }

    if (countPlayersFrom(address) >= config.getMaxPlayersPerIp()
        && config.getMaxPlayersPerIp() > 0) {
      logger.warn("Blocked too many active players from {}", address.getHostAddress());
      event.setResult(PreLoginComponentResult.denied(SECURITY_DENY));
    }
  }

  @Subscribe
  public void onProxyPing(ProxyPingEvent event) {
    SallyLabsPatchConfig config = server.getConfiguration().getSallyLabsPatchConfig();
    if (!config.isEnabled()) {
      return;
    }

    InetAddress address = event.getConnection().getRemoteAddress().getAddress();
    if (!pingLimiter.allow(address, config.getMaxPingsPerMinutePerIp())) {
      logger.warn("Dropped ping flood from {}", address.getHostAddress());
      event.setResult(GenericResult.denied());
    }
  }

  @Subscribe
  public void onPreTransfer(PreTransferEvent event) {
    if (server.getConfiguration().getSallyLabsPatchConfig().isBlockTransfers()) {
      logger.warn("Blocked transfer packet for {} to {}", event.player(),
          event.originalAddress());
      event.setResult(PreTransferEvent.TransferResult.denied());
    }
  }

  @Subscribe
  public void onCommandExecute(CommandExecuteEvent event) {
    SallyLabsPatchConfig config = server.getConfiguration().getSallyLabsPatchConfig();
    if (!config.isBlockDangerousCommands() || !(event.getCommandSource() instanceof Player)) {
      return;
    }

    String normalized = normalizeCommand(event.getCommand());
    for (String blocked : config.getBlockedCommands()) {
      String normalizedBlocked = normalizeCommand(blocked);
      if (normalized.equals(normalizedBlocked) || normalized.startsWith(normalizedBlocked + " ")) {
        event.getCommandSource().sendMessage(COMMAND_DENY);
        logger.warn("Blocked proxy command '{}' from {}", normalized, event.getCommandSource());
        event.setResult(CommandResult.denied());
        return;
      }
    }
  }

  private int countPlayersFrom(InetAddress address) {
    int count = 0;
    for (Player player : server.getAllPlayers()) {
      if (player.getRemoteAddress().getAddress().equals(address)) {
        count++;
      }
    }
    return count;
  }

  private static boolean isUsernameAllowed(String username, String pattern) {
    try {
      return Pattern.matches(pattern, username);
    } catch (PatternSyntaxException ignored) {
      return Pattern.matches(SallyLabsPatchConfig.DEFAULT.getUsernamePattern(), username);
    }
  }

  private boolean isVirtualHostAllowed(PreLoginEvent event, SallyLabsPatchConfig config) {
    Optional<String> virtualHost = event.getConnection().getVirtualHost()
        .map(InetSocketAddress::getHostString);
    return virtualHostSecurity.isAllowed(virtualHost, config);
  }

  private static boolean isVirtualHostShapeAllowed(PreLoginEvent event,
                                                   SallyLabsPatchConfig config) {
    String rawHost = event.getConnection().getRawVirtualHost().orElse("");
    if (config.isRequireVirtualHost() && rawHost.isBlank()) {
      return false;
    }
    return config.getMaxVirtualHostLength() <= 0
        || rawHost.length() <= config.getMaxVirtualHostLength();
  }

  private static String normalizeCommand(String command) {
    return command.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }
}
