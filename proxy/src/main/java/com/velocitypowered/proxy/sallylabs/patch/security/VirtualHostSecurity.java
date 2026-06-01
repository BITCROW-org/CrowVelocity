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

import com.velocitypowered.proxy.sallylabs.patch.config.SallyLabsPatchConfig;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Validates client handshake hostnames before login continues.
 */
public final class VirtualHostSecurity {

  private static final Pattern IPV4_LITERAL = Pattern.compile("\\d{1,3}(?:\\.\\d{1,3}){3}");
  private static final Pattern HEX_OR_COLON = Pattern.compile("[0-9a-f:]+", Pattern.CASE_INSENSITIVE);

  public boolean isAllowed(Optional<String> virtualHost, SallyLabsPatchConfig config) {
    if (!config.isEnforceVirtualHostDomains()) {
      return true;
    }

    String host = virtualHost.map(VirtualHostSecurity::normalizeHost).orElse("");
    if (host.isBlank() || isNumericAddress(host) || config.getAllowedVirtualHosts().isEmpty()) {
      return false;
    }

    return config.getAllowedVirtualHosts().stream()
        .map(VirtualHostSecurity::normalizeHost)
        .anyMatch(allowed -> host.equals(allowed)
            || config.isAllowVirtualHostSubdomains() && host.endsWith("." + allowed));
  }

  private static String normalizeHost(String host) {
    String normalized = host.trim().toLowerCase(Locale.ROOT);
    if (normalized.endsWith(".")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private static boolean isNumericAddress(String host) {
    return IPV4_LITERAL.matcher(host).matches()
        || host.contains(":") && HEX_OR_COLON.matcher(host).matches();
  }
}
