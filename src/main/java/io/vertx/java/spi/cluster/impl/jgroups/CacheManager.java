package io.vertx.java.spi.cluster.impl.jgroups;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.domain.SyncMapWrapper;
import io.vertx.java.spi.cluster.impl.jgroups.domain.async.AsyncMapWrapper;
import io.vertx.java.spi.cluster.impl.jgroups.domain.async.AsyncMultiMapWrapper;
import io.vertx.java.spi.cluster.impl.jgroups.domain.MultiMapImpl;
import io.vertx.java.spi.cluster.impl.jgroups.services.*;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.*;
import org.jgroups.blocks.RpcDispatcher;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager extends ReceiverAdapter implements LambdaLogger {

  private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

  private final VertxSPI vertx;
  private final JChannel channel;
  private final RpcDispatcher dispatcher;

  private final Map<String, Map> maps = new ConcurrentHashMap<>();
  private final Map<String, MultiMapImpl> multiMaps = new ConcurrentHashMap<>();

  private final RpcExecutorService executorService;
  private final RpcMultiMapService multiMapService;
  private final RpcMapService mapService;

  public CacheManager(VertxSPI vertx, JChannel channel) {
    this.vertx = vertx;
    this.channel = channel;

    this.multiMapService = new DefaultRpcMultiMapService(multiMaps);
    this.mapService = new DefaultRpcMapService(maps);

    RpcServerObjDelegate server_obj = new RpcServerObjDelegate(mapService, multiMapService);
    this.dispatcher = new RpcDispatcher(this.channel, this, null, server_obj);
    this.dispatcher.setMethodLookup(server_obj.getMethodLookup());

    this.executorService = new DefaultRpcExecutorService(vertx, dispatcher);
  }

  public <K, V> void createAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
    logDebug(() -> String.format("method createAsyncMultiMap address[%s] name[%s]", channel.getAddressAsString(), name));
    executorService.remoteExecute(RpcServerObjDelegate.CALL_MULTIMAP_CREATE.method(name),
        (result) -> {
          logDebug(() -> String.format("method created AsyncMultiMap address[%s] name[%s]", channel.getAddressAsString(), name));
          if (result.succeeded()) {
            AsyncMultiMapWrapper<K, V> wrapper = new AsyncMultiMapWrapper<K, V>(name, multiMaps.<String, MultiMapImpl<K, V>>get(name), executorService);
            handler.handle(Future.completedFuture(wrapper));
          } else {
            handler.handle(Future.completedFuture(result.cause()));
          }
        });
  }

  public <K, V> void createAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
    logDebug(() -> String.format("method createAsyncMap address[%s] name[%s]", channel.getAddressAsString(), name));
    executorService.remoteExecute(RpcServerObjDelegate.CALL_MAP_CREATE.method(name),
        (result) -> {
          if (result.succeeded()) {
            AsyncMapWrapper<K, V> wrapper = new AsyncMapWrapper<K, V>(name, maps.<String, Map<K, V>>get(name), vertx, executorService);
            handler.handle(Future.completedFuture(wrapper));
          } else {
            handler.handle(Future.completedFuture(result.cause()));
          }
        });
  }

  public <K, V> Map<K, V> createSyncMap(String name) {
    return new SyncMapWrapper<>(name, (Map<K, V>) maps.computeIfAbsent(name, (key) -> new HashMap()), executorService);
  }

  @Override
  public Logger log() {
    return LOG;
  }

  @Override
  public void getState(OutputStream output) throws Exception {
    System.out.println("CacheManager.getState");
    System.out.println("output = [" + output + "]");
    try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(output, 1024);
         ObjectOutputStream oos = new ObjectOutputStream(bufferedOutputStream)) {
      oos.writeObject(multiMaps);
      oos.writeObject(maps);
      oos.flush();
    }
  }

  @Override
  public void setState(InputStream input) throws Exception {
    System.out.println("CacheManager.setState");
    System.out.println("input = [" + input + "]");
    try (ObjectInputStream oos = new ObjectInputStream(input)) {
      multiMaps.putAll((Map<String, MultiMapImpl>) oos.readObject());
      maps.putAll((Map<String, Map>) oos.readObject());
    }
  }

  public void start() {
    try {
      channel.getState(null, 10000);
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }
}
