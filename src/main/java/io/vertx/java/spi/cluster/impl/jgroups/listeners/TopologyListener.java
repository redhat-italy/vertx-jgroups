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

package io.vertx.java.spi.cluster.impl.jgroups.listeners;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.Action;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TopologyListener extends ReceiverAdapter implements LambdaLogger {

  private final static Logger LOG = LoggerFactory.getLogger(TopologyListener.class);
  private final String name;
  private final VertxSPI vertx;

  private ViewId viewId;
  private List<Address> members = Collections.emptyList();
  private Optional<NodeListener> nodeListener = Optional.empty();

  public TopologyListener(VertxSPI vertx, String name) {
    logDebug(() -> "[" + name + "] - Start topology listener");
    this.vertx = vertx;
    this.name = name;
  }

  @Override
  public void receive(Message msg) {
    System.out.println("[" + name + "] Message receive [" + msg + "]");
  }

  @Override
  public void viewAccepted(View view) {
    if (view.getViewId() == null) {
      logTrace(() -> "[" + name + "] - Called View accepted [" + view + "] with ViewId null.");
      return;
    }

    if (viewId != null && view.getViewId().compareToIDs(viewId) <= 0) {
      logTrace(() -> "[" + name + "] - Called View accepted [" + view + "] but there's no changes.");
      return;
    }
    List<Address> oldMembers = members;
    members = view.getMembers();
    viewId = view.getViewId().copy();

    nodeListener.ifPresent((listener) -> vertx.executeBlocking(() -> {
      List<Address> newMembers = members;

      Predicate<Address> oldMemberNodeHasLeft = (member) -> !newMembers.contains(member);
      Predicate<Address> newMemberNodeJoin = (member) -> !oldMembers.contains(member);
      Consumer<Address> nodeJoin = (member) -> {
        logInfo(() -> "[" + name + "] - Notify node [" + member + "] has joined the cluster");
        listener.nodeAdded(member.toString());
      };
      Consumer<Address> nodeLeft = (member) -> {
        logInfo(() -> "[" + name + "] - Notify node [" + member + "] has left the cluster");
        listener.nodeAdded(member.toString());
      };

      newMembers.stream()
          .filter(newMemberNodeJoin)
          .forEach(nodeJoin);
      oldMembers.stream()
          .filter(oldMemberNodeHasLeft)
          .forEach(nodeLeft);

      return null;
    }, (r) -> Function.identity()));
  }

  public void setNodeListener(NodeListener nodeListener) {
    logDebug(() -> "[" + name + "] - Set topology listener [" + nodeListener + "]");
    this.nodeListener = Optional.of(nodeListener);
  }

  public List<String> getNodes() {
    logDebug(() -> "[" + name + "] - Get Nodes from topology [" + members + "]");
    return members
        .stream()
        .map(Address::toString)
        .collect(Collectors.toList());
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
