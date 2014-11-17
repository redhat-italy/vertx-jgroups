package io.vertx.java.spi.cluster.impl.jgroups.services;

public interface RpcMultiMapService {

  boolean multiMapCreate(String name);

  <K, V> void multiMapAdd(String name, K k, V v);

  <K, V> boolean multiMapRemove(String name, K k, V v);

}
