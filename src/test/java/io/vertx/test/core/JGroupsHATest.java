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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.java.spi.cluster.impl.jgroups.JGroupsClusterManager;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class JGroupsHATest extends HATest {

  @Override
  protected ClusterManager getClusterManager() {
    return new JGroupsClusterManager();
  }

  @Test
  public void testFailed_QuorumLost() throws Exception {
    Vertx vertx1 = startVertx(3);
    Vertx vertx2 = startVertx(3);
    Vertx vertx3 = startVertx(3);
    DeploymentOptions options = new DeploymentOptions().setHa(true);
    JsonObject config = new JsonObject().put("foo", "bar");
    options.setConfig(config);
    vertx1.deployVerticle("java:" + HAVerticle1.class.getName(), options, ar -> {
      assertTrue(ar.succeeded());
      assertTrue(vertx1.deployments().contains(ar.result()));
      ;
    });
    vertx2.deployVerticle("java:" + HAVerticle2.class.getName(), options, ar -> {
      assertTrue(ar.succeeded());
      assertTrue(vertx2.deployments().contains(ar.result()));
      ;
    });
    waitUntil(() -> vertx1.deployments().size() == 1 && vertx2.deployments().size() == 1);
    // Now close vertx3 - quorum should then be lost and verticles undeployed
    CountDownLatch latch = new CountDownLatch(1);
    vertx3.close(ar -> {
      latch.countDown();
    });
    awaitLatch(latch);
    waitUntil(() -> vertx1.deployments().isEmpty() && vertx2.deployments().isEmpty());
    // Now re-instate the quorum
    Vertx vertx4 = startVertx(3);
    waitUntil(() -> vertx1.deployments().size() == 1 && vertx2.deployments().size() == 1);

    closeVertices(vertx1, vertx2, vertx4);
  }

}
