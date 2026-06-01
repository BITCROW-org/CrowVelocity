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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.junit.jupiter.api.Test;

class SallyLabsPatchConfigTest {

  @Test
  void readsNestedSallyLabsTomlShape() {
    CommentedConfig root = CommentedConfig.inMemory();
    CommentedConfig general = CommentedConfig.inMemory();
    CommentedConfig stability = CommentedConfig.inMemory();
    CommentedConfig security = CommentedConfig.inMemory();
    CommentedConfig pluginMessages = CommentedConfig.inMemory();
    CommentedConfig forwarding = CommentedConfig.inMemory();

    general.set("enabled", true);
    stability.set("server-switch-cooldown-millis", 1200);
    pluginMessages.set("max-payload-bytes", 2048);
    pluginMessages.set("block-legacy-bungee", false);
    forwarding.set("require-secure-forwarding", true);
    security.set("plugin-messages", pluginMessages);
    security.set("forwarding", forwarding);
    root.set("general", general);
    root.set("stability", stability);
    root.set("security", security);

    SallyLabsPatchConfig config = SallyLabsPatchConfig.fromConfig(root);

    assertTrue(config.isEnabled());
    assertEquals(1200, config.getServerSwitchCooldownMillis());
    assertEquals(2048, config.getMaxPluginMessageBytes());
    assertFalse(config.isBlockLegacyBungeePluginMessages());
    assertTrue(config.isRequireSecureForwarding());
  }
}
