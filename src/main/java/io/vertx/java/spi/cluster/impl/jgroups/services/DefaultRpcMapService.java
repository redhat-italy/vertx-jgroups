package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultRpcMapService implements RpcMapService, LambdaLogger {

  private final static Logger LOG = LoggerFactory.getLogger(DefaultRpcMapService.class);

  private final Map<String, Map> maps;

  public DefaultRpcMapService(Map<String, Map> maps) {
    this.maps = maps;
  }

  private <K, V> void execute(String name, Consumer<Map<K, V>> consumer) {
    this.<K, V, Void>executeAndReturn(name, (map) -> {
      consumer.accept(map);
      return null;
    });
  }

  public <K, V, R> R executeAndReturn(String name, Function<Map<K, V>, R> function) {
    return function.apply((Map<K, V>) maps.computeIfAbsent(name, (k) -> new ConcurrentHashMap()));
/*
    Map map = Optional
        .ofNullable(maps.get(name))
        .orElseThrow(() -> new IllegalStateException(String.format("Map [%s] not found.", name)));
    return function.apply((Map<K, V>) map);
*/
  }

  public <K, V> Map<K, V> mapGet(String name) {
    return this.<K, V, Map<K, V>>executeAndReturn(name, Function.identity());
  }

  @Override
  public <K, V> boolean mapCreate(String name) {
    logDebug(() -> String.format("method mapCreate name[%s]", name));
    maps.computeIfAbsent(name, (key) -> new ConcurrentHashMap());
    return true;
  }

  @Override
  public <K, V> void mapPut(String name, K k, V v) {
    logDebug(() -> "RpcMapService.put name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    this.<K, V>execute(name, (map) -> map.put(k, v));
  }

  @Override
  public <K, V> V mapPutIfAbsent(String name, K k, V v) {
    logTrace(() -> "RpcMapService.putIfAbsent name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    return this.<K, V, V>executeAndReturn(name, (map) -> map.putIfAbsent(k, v));
  }

  @Override
  public <K, V> V mapRemove(String name, K k) {
    logTrace(() -> "RpcMapService.remove name = [" + name + "], k = [" + k + "]");
    return this.<K, V, V>executeAndReturn(name, (map) -> map.remove(k));
  }

  @Override
  public <K, V> boolean mapRemoveIfPresent(String name, K k, V v) {
    logTrace(() -> "RpcMapService.removeIfPresent name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    return this.<K, V, Boolean>executeAndReturn(name, (map) -> map.remove(k, v));
  }

  @Override
  public <K, V> V mapReplace(String name, K k, V v) {
    logTrace(() -> "RpcMapService.replace name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    return this.<K, V, V>executeAndReturn(name, (map) -> map.replace(k, v));
  }

  @Override
  public <K, V> boolean mapReplaceIfPresent(String name, K k, V oldValue, V newValue) {
    logTrace(() -> "RpcMapService.removeIfPresent name = [" + name + "], k = [" + k + "], oldValue = [" + oldValue + "], newValue = [" + newValue + "]");
    return this.<K, V, Boolean>executeAndReturn(name, (map) -> map.replace(k, oldValue, newValue));
  }

  @Override
  public <K, V> void mapClear(String name) {
    logTrace(() -> "RpcMapService.clear name = [" + name + "]");
    this.<K, V>execute(name, (map) -> map.clear());
  }

  @Override
  public <K, V> void mapPutAll(String name, Map<K, V> m) {
    logTrace(() -> "RpcMapService.mapPutAll name = [" + name + "]");
    this.execute(name, (map) -> map.putAll(m));
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
