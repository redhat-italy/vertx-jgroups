/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.test.core;

import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.java.spi.cluster.impl.jgroups.JGroupsClusterManager;
import org.junit.Test;

public class JGroupsClusteredAsynchronousLockTest extends ClusteredAsynchronousLockTest {

    @Override
    protected ClusterManager getClusterManager() {
        return new JGroupsClusterManager();
    }


  @Test
  public void testAcquireMio() {
    getVertx().sharedData().getLock("foo", ar -> {
      assertTrue(ar.succeeded());
      long start = System.currentTimeMillis();
      Lock lock = ar.result();
      vertx.setTimer(1000, tid -> {
        lock.release();
      });
      getVertx().sharedData().getLock("foo", ar2 -> {
        assertTrue(ar2.succeeded());
        // Should be delayed
        assertTrue(System.currentTimeMillis() - start >= 1000);
        testComplete();
      });
    });
    await();
  }

}
