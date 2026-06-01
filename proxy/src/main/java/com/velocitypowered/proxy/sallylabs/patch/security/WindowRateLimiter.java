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

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple fixed-window limiter keyed by remote IP address.
 */
public final class WindowRateLimiter {

  private static final int CLEANUP_THRESHOLD = 4096;

  private final long windowMillis;
  private final Map<InetAddress, Window> windows = new ConcurrentHashMap<>();

  public WindowRateLimiter(long windowMillis) {
    this.windowMillis = windowMillis;
  }

  public boolean allow(InetAddress address, int limit) {
    if (limit <= 0) {
      return true;
    }

    long now = System.currentTimeMillis();
    if (windows.size() > CLEANUP_THRESHOLD) {
      cleanupExpired(now);
    }

    Window window = windows.computeIfAbsent(address, ignored -> new Window(now));
    synchronized (window) {
      if (now - window.startedAt >= windowMillis) {
        window.startedAt = now;
        window.count = 0;
      }
      window.count++;
      return window.count <= limit;
    }
  }

  private void cleanupExpired(long now) {
    windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= windowMillis);
  }

  private static final class Window {
    private long startedAt;
    private int count;

    private Window(long startedAt) {
      this.startedAt = startedAt;
    }
  }
}
