package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.java.spi.cluster.impl.jgroups.domain.MultiMapImpl;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DefaultRpcMultiMapService implements RpcMultiMapService, LambdaLogger {

  private final static Logger LOG = LoggerFactory.getLogger(DefaultRpcMultiMapService.class);

  private final Map<String, MultiMapImpl> maps;

  public DefaultRpcMultiMapService(Map<String, MultiMapImpl> maps) {
    this.maps = maps;
  }

  public <K, V, R> R executeAndReturn(String name, Function<MultiMapImpl<K, V>, R> function) {
    MultiMapImpl map = Optional
        .ofNullable(maps.get(name))
        .orElseThrow(() -> new IllegalStateException(String.format("MultiMapImpl [%s] not found.", name)));
    return function.apply((MultiMapImpl<K, V>)map);
  }

  public <K, V> MultiMapImpl<K, V> multiMapGet(String name) {
    return this.<K, V, MultiMapImpl<K,V>>executeAndReturn(name, Function.identity());
  }

  public boolean multiMapCreate(String name) {
    logTrace(() -> String.format("method mapCreate name[%s]", name));
    maps.computeIfAbsent(name, (key) -> new MultiMapImpl());
    return true;
  }

  public <K, V> void multiMapAdd(String name, K k, V v) {
    logTrace(() -> String.format("method multiMapAdd name[%s] key[%s] value[%s]", name, k, v));
    this.<K, V, Void>executeAndReturn(name, (map) -> {
      map.add(k, v);
      return null;
    });
  }

  public <K, V> boolean multiMapRemove(String name, K k, V v) {
    logTrace(() -> String.format("method multiMapRemove name[%s] key[%s] value[%s]", name, k, v));
    return this.<K, V, Boolean>executeAndReturn(name, (map) -> map.remove(k, v));
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
