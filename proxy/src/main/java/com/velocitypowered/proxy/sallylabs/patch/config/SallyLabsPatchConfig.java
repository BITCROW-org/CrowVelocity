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

package com.velocitypowered.proxy.sallylabs.patch.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the SallyLabs proxy hardening patches.
 */
public final class SallyLabsPatchConfig {

  public static final SallyLabsPatchConfig DEFAULT = new SallyLabsPatchConfig(
      true,
      128,
      1048576,
      64,
      true,
      true,
      false,
      "limbo",
      5000,
      ImmutableList.of("lobby"),
      750,
      8,
      60,
      4,
      true,
      true,
      "^[A-Za-z0-9_]{3,16}$",
      255,
      false,
      false,
      true,
      ImmutableList.of(),
      ImmutableList.of("velocity plugins", "velocity dump", "velocity heap", "velocity reload"),
      ImmutableList.of("bungeecord", "bungee:main"),
      true,
      false
  );

  private final boolean enabled;
  private final int maxConcurrentInitialConnects;
  private final int maxPluginMessageBytes;
  private final int maxPluginMessageChannelLength;
  private final boolean blockLegacyBungeePluginMessages;
  private final boolean limboEnabled;
  private final boolean internalLimbo;
  private final String limboServer;
  private final int limboRetryIntervalMillis;
  private final List<String> limboRecoveryServers;
  private final int serverSwitchCooldownMillis;
  private final int maxLoginsPerMinutePerIp;
  private final int maxPingsPerMinutePerIp;
  private final int maxPlayersPerIp;
  private final boolean blockTransfers;
  private final boolean blockDangerousCommands;
  private final String usernamePattern;
  private final int maxVirtualHostLength;
  private final boolean requireVirtualHost;
  private final boolean enforceVirtualHostDomains;
  private final boolean allowVirtualHostSubdomains;
  private final List<String> allowedVirtualHosts;
  private final List<String> blockedCommands;
  private final List<String> blockedPluginMessageChannels;
  private final boolean warnOnInsecureForwarding;
  private final boolean requireSecureForwarding;

  private SallyLabsPatchConfig(boolean enabled, int maxConcurrentInitialConnects,
                               int maxPluginMessageBytes, int maxPluginMessageChannelLength,
                               boolean blockLegacyBungeePluginMessages,
                               boolean limboEnabled, boolean internalLimbo, String limboServer,
                               int limboRetryIntervalMillis, List<String> limboRecoveryServers,
                               int serverSwitchCooldownMillis,
                               int maxLoginsPerMinutePerIp, int maxPingsPerMinutePerIp,
                               int maxPlayersPerIp, boolean blockTransfers,
                               boolean blockDangerousCommands, String usernamePattern,
                               int maxVirtualHostLength, boolean requireVirtualHost,
                               boolean enforceVirtualHostDomains,
                               boolean allowVirtualHostSubdomains,
                               List<String> allowedVirtualHosts, List<String> blockedCommands,
                               List<String> blockedPluginMessageChannels,
                               boolean warnOnInsecureForwarding,
                               boolean requireSecureForwarding) {
    this.enabled = enabled;
    this.maxConcurrentInitialConnects = maxConcurrentInitialConnects;
    this.maxPluginMessageBytes = maxPluginMessageBytes;
    this.maxPluginMessageChannelLength = maxPluginMessageChannelLength;
    this.blockLegacyBungeePluginMessages = blockLegacyBungeePluginMessages;
    this.limboEnabled = limboEnabled;
    this.internalLimbo = internalLimbo;
    this.limboServer = limboServer;
    this.limboRetryIntervalMillis = limboRetryIntervalMillis;
    this.limboRecoveryServers = ImmutableList.copyOf(limboRecoveryServers);
    this.serverSwitchCooldownMillis = serverSwitchCooldownMillis;
    this.maxLoginsPerMinutePerIp = maxLoginsPerMinutePerIp;
    this.maxPingsPerMinutePerIp = maxPingsPerMinutePerIp;
    this.maxPlayersPerIp = maxPlayersPerIp;
    this.blockTransfers = blockTransfers;
    this.blockDangerousCommands = blockDangerousCommands;
    this.usernamePattern = usernamePattern;
    this.maxVirtualHostLength = maxVirtualHostLength;
    this.requireVirtualHost = requireVirtualHost;
    this.enforceVirtualHostDomains = enforceVirtualHostDomains;
    this.allowVirtualHostSubdomains = allowVirtualHostSubdomains;
    this.allowedVirtualHosts = ImmutableList.copyOf(allowedVirtualHosts);
    this.blockedCommands = ImmutableList.copyOf(blockedCommands);
    this.blockedPluginMessageChannels = ImmutableList.copyOf(blockedPluginMessageChannels);
    this.warnOnInsecureForwarding = warnOnInsecureForwarding;
    this.requireSecureForwarding = requireSecureForwarding;
  }

  public static SallyLabsPatchConfig read(Path path) throws IOException {
    URL defaultConfigLocation = SallyLabsPatchConfig.class.getClassLoader()
        .getResource("default-sallylabs.toml");
    if (defaultConfigLocation == null) {
      throw new RuntimeException("Default SallyLabs configuration file does not exist.");
    }

    try (CommentedFileConfig config = CommentedFileConfig.builder(path)
        .defaultData(defaultConfigLocation)
        .autosave()
        .preserveInsertionOrder()
        .sync()
        .build()) {
      config.load();
      return fromConfig(config);
    }
  }

  public static SallyLabsPatchConfig fromConfig(CommentedConfig config) {
    if (config == null) {
      return DEFAULT;
    }

    CommentedConfig generalConfig = child(config, "general");
    CommentedConfig stabilityConfig = child(config, "stability");
    CommentedConfig limboConfig = config.get("limbo");
    CommentedConfig securityConfig = config.get("security");
    CommentedConfig loginSecurityConfig = child(securityConfig, "login");
    CommentedConfig hostSecurityConfig = child(securityConfig, "host");
    CommentedConfig commandSecurityConfig = child(securityConfig, "commands");
    CommentedConfig pluginMessageSecurityConfig = child(securityConfig, "plugin-messages");
    CommentedConfig transferSecurityConfig = child(securityConfig, "transfer");
    CommentedConfig forwardingSecurityConfig = child(securityConfig, "forwarding");

    return new SallyLabsPatchConfig(
        booleanOrDefault(generalConfig, "enabled", DEFAULT.enabled),
        positiveOrDisable(stabilityConfig, "max-concurrent-initial-connects",
            DEFAULT.maxConcurrentInitialConnects),
        positiveOrDisable(pluginMessageSecurityConfig, "max-payload-bytes",
            DEFAULT.maxPluginMessageBytes),
        positiveOrDisable(pluginMessageSecurityConfig, "max-channel-length",
            DEFAULT.maxPluginMessageChannelLength),
        booleanOrDefault(pluginMessageSecurityConfig, "block-legacy-bungee",
            DEFAULT.blockLegacyBungeePluginMessages),
        limboConfig == null ? DEFAULT.limboEnabled
            : limboConfig.getOrElse("enabled", DEFAULT.limboEnabled),
        limboConfig == null ? DEFAULT.internalLimbo
            : limboConfig.getOrElse("internal", DEFAULT.internalLimbo),
        limboConfig == null ? DEFAULT.limboServer
            : limboConfig.getOrElse("server", DEFAULT.limboServer),
        limboConfig == null ? DEFAULT.limboRetryIntervalMillis
            : positiveOrDefault(limboConfig, "retry-interval-millis",
                DEFAULT.limboRetryIntervalMillis),
        limboConfig == null ? DEFAULT.limboRecoveryServers
            : stringListOrDefault(limboConfig, "recovery-servers", DEFAULT.limboRecoveryServers),
        positiveOrDisable(stabilityConfig, "server-switch-cooldown-millis",
            DEFAULT.serverSwitchCooldownMillis),
        positiveOrDisable(loginSecurityConfig, "max-logins-per-minute-per-ip",
            DEFAULT.maxLoginsPerMinutePerIp),
        positiveOrDisable(loginSecurityConfig, "max-pings-per-minute-per-ip",
            DEFAULT.maxPingsPerMinutePerIp),
        positiveOrDisable(loginSecurityConfig, "max-players-per-ip", DEFAULT.maxPlayersPerIp),
        booleanOrDefault(transferSecurityConfig, "block-transfers", DEFAULT.blockTransfers),
        booleanOrDefault(commandSecurityConfig, "block-dangerous-commands",
            DEFAULT.blockDangerousCommands),
        stringOrDefault(loginSecurityConfig, "username-pattern", DEFAULT.usernamePattern),
        positiveOrDisable(hostSecurityConfig, "max-virtual-host-length",
            DEFAULT.maxVirtualHostLength),
        booleanOrDefault(hostSecurityConfig, "require-virtual-host", DEFAULT.requireVirtualHost),
        booleanOrDefault(hostSecurityConfig, "enforce-hostnames",
            DEFAULT.enforceVirtualHostDomains),
        booleanOrDefault(hostSecurityConfig, "allow-subdomains",
            DEFAULT.allowVirtualHostSubdomains),
        stringListOrDefault(hostSecurityConfig, "allowed-virtual-hosts",
            DEFAULT.allowedVirtualHosts),
        stringListOrDefault(commandSecurityConfig, "blocked-commands", DEFAULT.blockedCommands),
        stringListOrDefault(pluginMessageSecurityConfig, "blocked-channels",
                DEFAULT.blockedPluginMessageChannels),
        booleanOrDefault(forwardingSecurityConfig, "warn-on-insecure-forwarding",
            DEFAULT.warnOnInsecureForwarding),
        booleanOrDefault(forwardingSecurityConfig, "require-secure-forwarding",
            DEFAULT.requireSecureForwarding)
    );
  }

  private static int positiveOrDisable(UnmodifiableConfig config, String key, int fallback) {
    if (config == null) {
      return fallback;
    }
    int value = config.getIntOrElse(key, fallback);
    return Math.max(value, -1);
  }

  private static int positiveOrDefault(UnmodifiableConfig config, String key, int fallback) {
    if (config == null) {
      return fallback;
    }
    int value = config.getIntOrElse(key, fallback);
    return value > 0 ? value : fallback;
  }

  private static List<String> stringListOrDefault(UnmodifiableConfig config, String key,
                                                  List<String> fallback) {
    if (config == null) {
      return fallback;
    }
    Object value = config.get(key);
    if (!(value instanceof List<?> rawList)) {
      return fallback;
    }

    List<String> result = new ArrayList<>();
    for (Object entry : rawList) {
      if (entry instanceof String serverName && !serverName.isBlank()) {
        result.add(serverName);
      }
    }
    return result.isEmpty() ? fallback : result;
  }

  private static CommentedConfig child(UnmodifiableConfig config, String key) {
    if (config == null) {
      return null;
    }
    Object value = config.get(key);
    return value instanceof CommentedConfig commented ? commented : null;
  }

  private static boolean booleanOrDefault(UnmodifiableConfig config, String key,
                                          boolean fallback) {
    return config == null ? fallback : config.getOrElse(key, fallback);
  }

  private static String stringOrDefault(UnmodifiableConfig config, String key, String fallback) {
    return config == null ? fallback : config.getOrElse(key, fallback);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getMaxConcurrentInitialConnects() {
    return maxConcurrentInitialConnects;
  }

  public int getMaxPluginMessageBytes() {
    return maxPluginMessageBytes;
  }

  public int getMaxPluginMessageChannelLength() {
    return maxPluginMessageChannelLength;
  }

  public boolean isBlockLegacyBungeePluginMessages() {
    return enabled && blockLegacyBungeePluginMessages;
  }

  public boolean isLimboEnabled() {
    return enabled && limboEnabled;
  }

  public boolean isInternalLimbo() {
    return isLimboEnabled() && internalLimbo;
  }

  public String getLimboServer() {
    return limboServer;
  }

  public int getLimboRetryIntervalMillis() {
    return limboRetryIntervalMillis;
  }

  public List<String> getLimboRecoveryServers() {
    return limboRecoveryServers;
  }

  public int getServerSwitchCooldownMillis() {
    return serverSwitchCooldownMillis;
  }

  public int getMaxLoginsPerMinutePerIp() {
    return maxLoginsPerMinutePerIp;
  }

  public int getMaxPingsPerMinutePerIp() {
    return maxPingsPerMinutePerIp;
  }

  public int getMaxPlayersPerIp() {
    return maxPlayersPerIp;
  }

  public boolean isBlockTransfers() {
    return enabled && blockTransfers;
  }

  public boolean isBlockDangerousCommands() {
    return enabled && blockDangerousCommands;
  }

  public String getUsernamePattern() {
    return usernamePattern;
  }

  public int getMaxVirtualHostLength() {
    return maxVirtualHostLength;
  }

  public boolean isRequireVirtualHost() {
    return enabled && requireVirtualHost;
  }

  public boolean isEnforceVirtualHostDomains() {
    return enabled && enforceVirtualHostDomains;
  }

  public boolean isAllowVirtualHostSubdomains() {
    return allowVirtualHostSubdomains;
  }

  public List<String> getAllowedVirtualHosts() {
    return allowedVirtualHosts;
  }

  public List<String> getBlockedCommands() {
    return blockedCommands;
  }

  public List<String> getBlockedPluginMessageChannels() {
    return blockedPluginMessageChannels;
  }

  public boolean isWarnOnInsecureForwarding() {
    return enabled && warnOnInsecureForwarding;
  }

  public boolean isRequireSecureForwarding() {
    return enabled && requireSecureForwarding;
  }

  @Override
  public String toString() {
    return "SallyLabsPatchConfig{"
        + "enabled=" + enabled
        + ", maxConcurrentInitialConnects=" + maxConcurrentInitialConnects
        + ", maxPluginMessageBytes=" + maxPluginMessageBytes
        + ", maxPluginMessageChannelLength=" + maxPluginMessageChannelLength
        + ", blockLegacyBungeePluginMessages=" + blockLegacyBungeePluginMessages
        + ", limboEnabled=" + limboEnabled
        + ", internalLimbo=" + internalLimbo
        + ", limboServer='" + limboServer + '\''
        + ", limboRetryIntervalMillis=" + limboRetryIntervalMillis
        + ", limboRecoveryServers=" + limboRecoveryServers
        + ", serverSwitchCooldownMillis=" + serverSwitchCooldownMillis
        + ", maxLoginsPerMinutePerIp=" + maxLoginsPerMinutePerIp
        + ", maxPingsPerMinutePerIp=" + maxPingsPerMinutePerIp
        + ", maxPlayersPerIp=" + maxPlayersPerIp
        + ", blockTransfers=" + blockTransfers
        + ", blockDangerousCommands=" + blockDangerousCommands
        + ", usernamePattern='" + usernamePattern + '\''
        + ", maxVirtualHostLength=" + maxVirtualHostLength
        + ", requireVirtualHost=" + requireVirtualHost
        + ", enforceVirtualHostDomains=" + enforceVirtualHostDomains
        + ", allowVirtualHostSubdomains=" + allowVirtualHostSubdomains
        + ", allowedVirtualHosts=" + allowedVirtualHosts
        + ", blockedCommands=" + blockedCommands
        + ", blockedPluginMessageChannels=" + blockedPluginMessageChannels
        + ", warnOnInsecureForwarding=" + warnOnInsecureForwarding
        + ", requireSecureForwarding=" + requireSecureForwarding
        + '}';
  }
}
