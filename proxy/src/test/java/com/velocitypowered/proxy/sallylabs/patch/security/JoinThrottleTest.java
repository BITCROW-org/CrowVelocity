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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JoinThrottleTest {

  @Test
  void enforcesConcurrentLimit() {
    JoinThrottle throttle = new JoinThrottle();

    assertTrue(throttle.tryAcquire(2));
    assertTrue(throttle.tryAcquire(2));
    assertFalse(throttle.tryAcquire(2));
    assertEquals(2, throttle.getActiveInitialConnects());
  }

  @Test
  void releasesSlotsAndNeverGoesNegative() {
    JoinThrottle throttle = new JoinThrottle();

    assertTrue(throttle.tryAcquire(1));
    throttle.release();
    throttle.release();

    assertEquals(0, throttle.getActiveInitialConnects());
    assertTrue(throttle.tryAcquire(1));
  }
}
