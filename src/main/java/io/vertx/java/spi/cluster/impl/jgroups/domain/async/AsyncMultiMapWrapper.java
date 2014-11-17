package io.vertx.java.spi.cluster.impl.jgroups.domain.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.jgroups.domain.MultiMapImpl;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcExecutorService;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcServerObjDelegate;

public class AsyncMultiMapWrapper<K, V> implements AsyncMultiMap<K, V>, LambdaLogger {

  private final static Logger LOG = LoggerFactory.getLogger(AsyncMultiMapWrapper.class);

  private final String name;
  private final MultiMapImpl<K, V> map;
  private final RpcExecutorService executorService;

  public AsyncMultiMapWrapper(String name, MultiMapImpl<K, V> map, RpcExecutorService executorService) {
    this.name = name;
    this.map = map;
    this.executorService = executorService;
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
    logTrace(() -> "add k = [" + k + "], v = [" + v + "], handler = [" + handler + "]");
    executorService.<Void>remoteExecute(RpcServerObjDelegate.CALL_MULTIMAP_ADD.method(name, k, v), handler);
  }

  @Override
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    logTrace(() -> "get k = [" + k + "], handler = [" + handler + "]");
    executorService.asyncExecute(() -> {
      ImmutableChoosableSet<V> result = map.get(k);
      logDebug(() -> "get k = [" + k + "], value = [" + result + "]");
      return result;
    }, handler);
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    logTrace(() -> "remove k = [" + k + "], v = [" + v + "], handler = [" + handler + "]");
    executorService.<Boolean>remoteExecute(RpcServerObjDelegate.CALL_MULTIMAP_REMOVE.method(name, k, v), handler);
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
    throw new VertxException(new UnsupportedOperationException("Not yet implemented."));
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
