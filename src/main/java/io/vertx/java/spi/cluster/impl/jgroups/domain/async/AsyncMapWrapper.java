package io.vertx.java.spi.cluster.impl.jgroups.domain.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcExecutorService;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcServerObjDelegate;

import java.util.Map;

public class AsyncMapWrapper<K, V> implements AsyncMap<K, V> {

  private final static Logger log = LoggerFactory.getLogger(AsyncMapWrapper.class);

  private final String name;
  private final Map<K, V> map;
  private final RpcExecutorService executorService;

  public AsyncMapWrapper(String name, Map<K, V> map, RpcExecutorService executorService) {
    this.name = name;
    this.map = map;
    this.executorService = executorService;
  }

  @Override
  public void get(K k, Handler<AsyncResult<V>> handler) {
    executorService.asyncExecute(() -> map.get(k), handler);
  }

  @Override
  public void put(K k, V v, Handler<AsyncResult<Void>> handler) {
    executorService.<Void>remoteExecute(RpcServerObjDelegate.CALL_MAP_PUT.method(name, k, v), handler);
  }

  @Override
  public void put(K k, V v, long timeout, Handler<AsyncResult<Void>> handler) {
    executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_PUT.method(name, k, v), timeout, handler);
  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> handler) {
    executorService.<V>remoteExecute(RpcServerObjDelegate.CALL_MAP_PUTIFABSENT.method(name, k, v), handler);
  }

  @Override
  public void putIfAbsent(K k, V v, long timeout, Handler<AsyncResult<V>> handler) {
    executorService.<V>remoteExecute(RpcServerObjDelegate.CALL_MAP_PUTIFABSENT.method(name, k, v), timeout, handler);
  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> handler) {
    executorService.<V>remoteExecute(RpcServerObjDelegate.CALL_MAP_REMOVE.method(name, k), handler);
  }

  @Override
  public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    executorService.<Boolean>remoteExecute(RpcServerObjDelegate.CALL_MAP_REMOVEIFPRESENT.method(name, k, v), handler);
  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> handler) {
    executorService.<V>remoteExecute(RpcServerObjDelegate.CALL_MAP_REPLACE.method(name, k, v), handler);
  }

  @Override
  public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> handler) {
    executorService.<Boolean>remoteExecute(RpcServerObjDelegate.CALL_MAP_REPLACEIFPRESENT.method(name, k, oldValue, newValue), handler);
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> handler) {
    executorService.<Void>remoteExecute(RpcServerObjDelegate.CALL_MAP_CLEAR.method(name), handler);
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> handler) {
    executorService.asyncExecute(() -> map.size(), handler);
  }
}
