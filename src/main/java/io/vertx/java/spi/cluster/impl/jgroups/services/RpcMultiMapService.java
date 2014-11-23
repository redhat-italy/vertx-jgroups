package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.java.spi.cluster.impl.jgroups.support.DataHolder;

public interface RpcMultiMapService {

  boolean multiMapCreate(String name);

  <K, V> void multiMapAdd(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> boolean multiMapRemove(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> void multiMapRemoveAll(String name, DataHolder<V> v);
}
