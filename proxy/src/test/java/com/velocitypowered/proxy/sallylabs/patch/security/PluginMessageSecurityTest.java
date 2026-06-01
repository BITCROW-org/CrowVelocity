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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

class PluginMessageSecurityTest {

  private final PluginMessageSecurity security = new PluginMessageSecurity();

  @Test
  void allowsPayloadAtConfiguredLimit() {
    PluginMessagePacket packet = new PluginMessagePacket("minecraft:brand",
        Unpooled.wrappedBuffer(new byte[16]));

    assertTrue(security.isAllowed(packet, configWithPayloadLimit(16)));
  }

  @Test
  void rejectsPayloadOverConfiguredLimit() {
    PluginMessagePacket packet = new PluginMessagePacket("minecraft:brand",
        Unpooled.wrappedBuffer(new byte[17]));

    assertFalse(security.isAllowed(packet, configWithPayloadLimit(16)));
  }

  @Test
  void disabledLimitAllowsAnyPayload() {
    PluginMessagePacket packet = new PluginMessagePacket("minecraft:brand",
        Unpooled.wrappedBuffer(new byte[17]));

    assertTrue(security.isAllowed(packet, configWithPayloadLimit(-1)));
  }

  @Test
  void rejectsLegacyBungeeChannelByDefault() {
    PluginMessagePacket packet = new PluginMessagePacket("BungeeCord",
        Unpooled.wrappedBuffer(new byte[1]));

    assertFalse(security.isAllowed(packet, SallyLabsPatchConfig.DEFAULT));
  }

  @Test
  void rejectsLongPluginMessageChannel() {
    PluginMessagePacket packet = new PluginMessagePacket("minecraft:very_long_channel",
        Unpooled.wrappedBuffer(new byte[1]));

    CommentedConfig config = basePluginMessageConfig();
    pluginMessages(config).set("max-channel-length", 10);
    assertFalse(security.isAllowed(packet, SallyLabsPatchConfig.fromConfig(config)));
  }

  private static SallyLabsPatchConfig configWithPayloadLimit(int limit) {
    CommentedConfig config = basePluginMessageConfig();
    pluginMessages(config).set("max-payload-bytes", limit);
    return SallyLabsPatchConfig.fromConfig(config);
  }

  private static CommentedConfig basePluginMessageConfig() {
    CommentedConfig config = CommentedConfig.inMemory();
    CommentedConfig security = CommentedConfig.inMemory();
    CommentedConfig pluginMessages = CommentedConfig.inMemory();
    pluginMessages.set("block-legacy-bungee", false);
    security.set("plugin-messages", pluginMessages);
    config.set("security", security);
    return config;
  }

  private static CommentedConfig pluginMessages(CommentedConfig config) {
    CommentedConfig security = config.get("security");
    return security.get("plugin-messages");
  }
}
