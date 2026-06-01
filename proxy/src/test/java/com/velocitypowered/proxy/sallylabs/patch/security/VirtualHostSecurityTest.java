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
import com.google.common.collect.ImmutableList;
import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VirtualHostSecurityTest {

  private final VirtualHostSecurity security = new VirtualHostSecurity();

  @Test
  void disabledPolicyAllowsDirectIp() {
    assertTrue(security.isAllowed(Optional.of("127.0.0.1"), SallyLabsPatchConfig.DEFAULT));
  }

  @Test
  void enforcedPolicyRejectsDirectIp() {
    assertFalse(security.isAllowed(Optional.of("127.0.0.1"), enforcedConfig(true)));
  }

  @Test
  void enforcedPolicyAllowsConfiguredDomainAndSubdomain() {
    SallyLabsPatchConfig config = enforcedConfig(true);

    assertTrue(security.isAllowed(Optional.of("example.com"), config));
    assertTrue(security.isAllowed(Optional.of("play.example.com"), config));
  }

  @Test
  void enforcedPolicyCanRejectSubdomains() {
    SallyLabsPatchConfig config = enforcedConfig(false);

    assertTrue(security.isAllowed(Optional.of("example.com"), config));
    assertFalse(security.isAllowed(Optional.of("play.example.com"), config));
  }

  private static SallyLabsPatchConfig enforcedConfig(boolean allowSubdomains) {
    CommentedConfig root = CommentedConfig.inMemory();
    CommentedConfig security = CommentedConfig.inMemory();
    CommentedConfig host = CommentedConfig.inMemory();
    host.set("enforce-hostnames", true);
    host.set("allow-subdomains", allowSubdomains);
    host.set("allowed-virtual-hosts", ImmutableList.of("example.com"));
    security.set("host", host);
    root.set("security", security);
    return SallyLabsPatchConfig.fromConfig(root);
  }
}
