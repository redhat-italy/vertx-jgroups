package io.vertx.java.spi.cluster.impl.jgroups.domain;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcExecutorService;
import io.vertx.java.spi.cluster.impl.jgroups.services.RpcServerObjDelegate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SyncMapWrapper<K, V> implements Map<K, V> {

  private final static Logger log = LoggerFactory.getLogger(SyncMapWrapper.class);

  private final String name;
  private final Map<K, V> map;
  private final RpcExecutorService executorService;

  public SyncMapWrapper(String name, Map<K, V> map, RpcExecutorService executorService) {
    this.name = name;
    this.map = map;
    this.executorService = executorService;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public V put(K key, V value) {
    return executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_PUT.method(name, key, value));
  }

  @Override
  public V remove(Object key) {
    return executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_REMOVE.method(name, key));
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_PUTALL.method(name, m));
  }

  @Override
  public void clear() {
    executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_CLEAR.method(name));
  }

  @Override
  public Set<K> keySet() {
    return Collections.unmodifiableSet(map.keySet());
  }

  @Override
  public Collection<V> values() {
    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return Collections.unmodifiableSet(map.entrySet());
  }
}
