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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limits the number of simultaneous first backend connection attempts.
 */
public final class JoinThrottle {

  private final AtomicInteger activeInitialConnects = new AtomicInteger();

  public boolean tryAcquire(int limit) {
    if (limit <= 0) {
      return true;
    }

    while (true) {
      int current = activeInitialConnects.get();
      if (current >= limit) {
        return false;
      }
      if (activeInitialConnects.compareAndSet(current, current + 1)) {
        return true;
      }
    }
  }

  public void release() {
    activeInitialConnects.updateAndGet(value -> Math.max(0, value - 1));
  }

  public int getActiveInitialConnects() {
    return activeInitialConnects.get();
  }
}
