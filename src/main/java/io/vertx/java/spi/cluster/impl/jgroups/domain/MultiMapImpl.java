package io.vertx.java.spi.cluster.impl.jgroups.domain;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiMapImpl<K, V> implements Externalizable {

  private final static Logger log = LoggerFactory.getLogger(MultiMapImpl.class);

  private Map<K, ImmutableChoosableSet<V>> cache = new ConcurrentHashMap<>();

  public MultiMapImpl() {
  }

  public void add(K k, V v) {
    if(log.isTraceEnabled()) {
      log.trace("MultiMapImpl.add k = [" + k + "], v = [" + v + "]");
    }
    cache.compute(k, (key, oldValue) ->
            Optional.ofNullable(oldValue)
                .orElseGet(() -> ImmutableChoosableSet.emptySet)
                .add(v)
    );
  }

  public ImmutableChoosableSet<V> get(K k) {
    ImmutableChoosableSet<V> v = cache.get(k);
    if(log.isTraceEnabled()) {
      log.trace("MultiMapImpl.get k = [" + k + "], v = [" + v + "]");
    }
    return v;
  }

  public boolean remove(K k, V v) {
    if(log.isTraceEnabled()) {
      log.trace("MultiMapImpl.remove k = [" + k + "], v = [" + v + "]");
    }
    final boolean[] result = {false};
    cache.computeIfPresent(k, (key, oldValue) -> {
      result[0] = true;
      return oldValue.remove(v);
    });
    return result[0];
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(cache);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    cache = (Map<K, ImmutableChoosableSet<V>>) in.readObject();
  }

  @Override
  public String toString() {
    return "MultiMapImpl{" +
        "cache=" + cache +
        '}';
  }
}
