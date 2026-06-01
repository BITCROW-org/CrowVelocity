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

package com.velocitypowered.proxy.sallylabs.patch;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.sallylabs.patch.limbo.LimboRecoveryService;
import com.velocitypowered.proxy.sallylabs.patch.security.JoinThrottle;
import com.velocitypowered.proxy.sallylabs.patch.security.PluginMessageSecurity;

/**
 * Entrypoint for SallyLabs hardening patches.
 */
public final class SallyLabsPatchManager {

  private final JoinThrottle joinThrottle = new JoinThrottle();
  private final PluginMessageSecurity pluginMessageSecurity = new PluginMessageSecurity();
  private final LimboRecoveryService limboRecoveryService;

  public SallyLabsPatchManager(VelocityServer server) {
    this.limboRecoveryService = new LimboRecoveryService(server);
  }

  public JoinThrottle joinThrottle() {
    return joinThrottle;
  }

  public PluginMessageSecurity pluginMessageSecurity() {
    return pluginMessageSecurity;
  }

  public LimboRecoveryService limboRecovery() {
    return limboRecoveryService;
  }
}
