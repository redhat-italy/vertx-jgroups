package io.vertx.java.spi.cluster.impl.jgroups.services;

import java.util.Map;

public interface RpcMapService {

  <K, V> boolean mapCreate(String name);

  <K, V> void mapPut(String name, K k, V v);

  <K, V> V mapPutIfAbsent(String name, K k, V v);

  <K, V> V mapRemove(String name, K k);

  <K, V> boolean mapRemoveIfPresent(String name, K k, V v);

  <K, V> V mapReplace(String name, K k, V v);

  <K, V> boolean mapReplaceIfPresent(String name, K k, V oldValue, V newValue);

  <K, V> void mapClear(String name);

  <K, V> void mapPutAll(String name, Map<K, V> m);
}
