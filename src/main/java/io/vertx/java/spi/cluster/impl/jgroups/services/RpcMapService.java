package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.java.spi.cluster.impl.jgroups.support.DataHolder;

import java.util.Map;

public interface RpcMapService {

  <K, V> boolean mapCreate(String name);

  <K, V> void mapPut(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> DataHolder<V> mapPutIfAbsent(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> DataHolder<V> mapRemove(String name, DataHolder<K> k);

  <K, V> boolean mapRemoveIfPresent(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> DataHolder<V> mapReplace(String name, DataHolder<K> k, DataHolder<V> v);

  <K, V> boolean mapReplaceIfPresent(String name, DataHolder<K> k, DataHolder<V> oldValue, DataHolder<V> newValue);

  <K, V> void mapClear(String name);

  <K, V> void mapPutAll(String name, Map<DataHolder<K>, DataHolder<V>> m);
}
