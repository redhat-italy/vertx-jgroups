package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.blocks.MethodCall;
import org.jgroups.util.Rsp;

import java.util.Optional;
import java.util.function.Supplier;

public interface RpcExecutorService {

  <T> T remoteExecute(MethodCall action);

  <T> void remoteExecute(MethodCall action, Handler<AsyncResult<T>> handler);

  <T> void asyncExecute(Supplier<T> action, Handler<AsyncResult<T>> handler);
}
