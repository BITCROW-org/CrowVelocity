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

package com.velocitypowered.proxy.sallylabs.patch.limbo;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;
import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Moves players out of the limbo backend once a backend responds again.
 */
public final class LimboRecoveryService {

  private static final Logger logger = LogManager.getLogger(LimboRecoveryService.class);

  private final VelocityServer server;
  private final Map<UUID, ScheduledTask> recoveryTasks = new ConcurrentHashMap<>();

  public LimboRecoveryService(VelocityServer server) {
    this.server = server;
  }

  public Optional<RegisteredServer> getLimboServer() {
    SallyLabsPatchConfig config = server.getConfiguration().getSallyLabsPatchConfig();
    if (!config.isLimboEnabled() || config.isInternalLimbo()) {
      return Optional.empty();
    }
    return server.getServer(config.getLimboServer());
  }

  public boolean isInternalLimboEnabled() {
    return server.getConfiguration().getSallyLabsPatchConfig().isInternalLimbo();
  }

  public Optional<RegisteredServer> getLimboServerIfDifferent(ConnectedPlayer player) {
    Optional<RegisteredServer> limbo = getLimboServer();
    if (limbo.isEmpty()) {
      return Optional.empty();
    }

    RegisteredServer current = limbo.get();
    if (player.getConnectedServer() != null
        && player.getConnectedServer().getServerInfo().getName()
            .equalsIgnoreCase(current.getServerInfo().getName())) {
      return Optional.empty();
    }
    return limbo;
  }

  public void startRecovery(ConnectedPlayer player) {
    SallyLabsPatchConfig config = server.getConfiguration().getSallyLabsPatchConfig();
    if (!config.isLimboEnabled()) {
      return;
    }

    UUID uuid = player.getUniqueId();
    recoveryTasks.computeIfAbsent(uuid, ignored -> server.getScheduler()
        .buildTask(VelocityVirtualPlugin.INSTANCE, task -> tryRecover(player, task))
        .delay(config.getLimboRetryIntervalMillis(), TimeUnit.MILLISECONDS)
        .repeat(config.getLimboRetryIntervalMillis(), TimeUnit.MILLISECONDS)
        .schedule());
  }

  public void stopRecovery(ConnectedPlayer player) {
    ScheduledTask task = recoveryTasks.remove(player.getUniqueId());
    if (task != null) {
      task.cancel();
    }
  }

  private void tryRecover(ConnectedPlayer player, ScheduledTask task) {
    if (!player.isActive()) {
      stopRecovery(player);
      return;
    }

    Optional<ServerConnection> current = player.getCurrentServer();
    if (current.isEmpty() || !isLimbo(current.get().getServer())) {
      stopRecovery(player);
      return;
    }

    for (String serverName : server.getConfiguration().getSallyLabsPatchConfig()
        .getLimboRecoveryServers()) {
      Optional<RegisteredServer> candidate = server.getServer(serverName);
      if (candidate.isEmpty() || isLimbo(candidate.get())) {
        continue;
      }

      candidate.get().ping().whenComplete((ignored, throwable) -> {
        if (throwable != null || !player.isActive()) {
          return;
        }
        player.createConnectionRequest(candidate.get()).connectWithIndication()
            .thenAccept(success -> {
              if (Boolean.TRUE.equals(success)) {
                stopRecovery(player);
                player.sendMessage(Component.text("Dein Zielserver ist wieder erreichbar."));
              }
            })
            .exceptionally(ex -> {
              logger.debug("Limbo recovery attempt for {} failed", player, ex);
              return null;
            });
      });
      return;
    }
  }

  private boolean isLimbo(RegisteredServer server) {
    return server.getServerInfo().getName().equalsIgnoreCase(
        this.server.getConfiguration().getSallyLabsPatchConfig().getLimboServer());
  }
}
