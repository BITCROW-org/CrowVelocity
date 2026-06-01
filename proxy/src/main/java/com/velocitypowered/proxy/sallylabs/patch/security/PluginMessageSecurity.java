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

import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import java.util.Locale;

/**
 * Guards plugin message payloads before copying or forwarding them.
 */
public final class PluginMessageSecurity {

  public boolean isAllowed(PluginMessagePacket packet, SallyLabsPatchConfig config) {
    if (!config.isEnabled()) {
      return true;
    }

    String channel = packet.getChannel().toLowerCase(Locale.ROOT);
    if (config.getMaxPluginMessageChannelLength() > 0
        && channel.length() > config.getMaxPluginMessageChannelLength()) {
      return false;
    }

    if (config.getMaxPluginMessageBytes() > 0
        && packet.content().readableBytes() > config.getMaxPluginMessageBytes()) {
      return false;
    }

    if (config.isBlockLegacyBungeePluginMessages() && isLegacyBungeeChannel(channel)) {
      return false;
    }

    return config.getBlockedPluginMessageChannels().stream()
        .map(blocked -> blocked.toLowerCase(Locale.ROOT))
        .noneMatch(channel::equals);
  }

  private static boolean isLegacyBungeeChannel(String channel) {
    return "bungeecord".equals(channel) || "bungee:main".equals(channel);
  }
}
