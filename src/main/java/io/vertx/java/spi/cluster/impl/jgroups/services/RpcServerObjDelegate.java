package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.MethodLookup;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RpcServerObjDelegate implements RpcMapService, RpcMultiMapService, LambdaLogger {

  private static final Logger LOG = LoggerFactory.getLogger(RpcServerObjDelegate.class);

  private final RpcMapService mapService;
  private final RpcMultiMapService multiMapService;

  private static final short MULTIMAP_CREATE = 10;
  private static final short MULTIMAP_ADD = 11;
  private static final short MULTIMAP_REMOVE = 12;

  private static final short MAP_CREATE = 20;
  private static final short MAP_PUT = 21;
  private static final short MAP_PUTALL = 28;
  private static final short MAP_PUTIFABSENT = 22;
  private static final short MAP_REMOVE = 23;
  private static final short MAP_REMOVEIFPRESENT = 24;
  private static final short MAP_REPLACE = 25;
  private static final short MAP_REPLACEIFPRESENT = 26;
  private static final short MAP_CLEAR = 27;

  public static final MethodCallInterface.OneParameter CALL_MULTIMAP_CREATE = (name) -> new MethodCall(MULTIMAP_CREATE, name);
  public static final MethodCallInterface.ThreeParameters CALL_MULTIMAP_ADD = (name, p1, p2) -> new MethodCall(MULTIMAP_ADD, name, p1, p2);
  public static final MethodCallInterface.ThreeParameters CALL_MULTIMAP_REMOVE = (name, p1, p2) -> new MethodCall(MULTIMAP_REMOVE, name, p1, p2);

  public static final MethodCallInterface.OneParameter CALL_MAP_CREATE = (name) -> new MethodCall(MAP_CREATE, name);
  public static final MethodCallInterface.ThreeParameters CALL_MAP_PUT = (name, p1, p2) -> new MethodCall(MAP_PUT, name, p1, p2);
  public static final MethodCallInterface.TwoParameters CALL_MAP_PUTALL = (name, p1) -> new MethodCall(MAP_PUTIFABSENT, name, p1);
  public static final MethodCallInterface.ThreeParameters CALL_MAP_PUTIFABSENT = (name, p1, p2) -> new MethodCall(MAP_PUTIFABSENT, name, p1, p2);
  public static final MethodCallInterface.TwoParameters CALL_MAP_REMOVE = (name, p1) -> new MethodCall(MAP_REMOVE, name, p1);
  public static final MethodCallInterface.ThreeParameters CALL_MAP_REMOVEIFPRESENT = (name, p1, p2) -> new MethodCall(MAP_REMOVEIFPRESENT, name, p1, p2);
  public static final MethodCallInterface.ThreeParameters CALL_MAP_REPLACE = (name, p1, p2) -> new MethodCall(MAP_REPLACE, name, p1, p2);
  public static final MethodCallInterface.FourParameters CALL_MAP_REPLACEIFPRESENT = (name, p1, p2, p3) -> new MethodCall(MAP_REPLACEIFPRESENT, name, p1, p2, p3);
  public static final MethodCallInterface.OneParameter CALL_MAP_CLEAR = (name) -> new MethodCall(MAP_CLEAR, name);

  private static final Map<Short, Method> methods = new HashMap<>();
  static {
    try {
      methods.put(MULTIMAP_CREATE, RpcServerObjDelegate.class.getMethod("multiMapCreate", String.class));
      methods.put(MULTIMAP_ADD, RpcServerObjDelegate.class.getMethod("multiMapAdd", String.class, Object.class, Object.class));
      methods.put(MULTIMAP_REMOVE, RpcServerObjDelegate.class.getMethod("multiMapRemove", String.class, Object.class, Object.class));

      methods.put(MAP_CREATE, RpcServerObjDelegate.class.getMethod("mapCreate", String.class));
      methods.put(MAP_PUT, RpcServerObjDelegate.class.getMethod("mapPut", String.class, Object.class, Object.class));
      methods.put(MAP_PUTALL, RpcServerObjDelegate.class.getMethod("mapPutAll", String.class, Map.class));
      methods.put(MAP_PUTIFABSENT, RpcServerObjDelegate.class.getMethod("mapPutIfAbsent", String.class, Object.class, Object.class));
      methods.put(MAP_REMOVE, RpcServerObjDelegate.class.getMethod("mapRemove", String.class, Object.class));
      methods.put(MAP_REMOVEIFPRESENT, RpcServerObjDelegate.class.getMethod("mapRemoveIfPresent", String.class, Object.class, Object.class));
      methods.put(MAP_REPLACE, RpcServerObjDelegate.class.getMethod("mapReplace", String.class, Object.class, Object.class));
      methods.put(MAP_REPLACEIFPRESENT, RpcServerObjDelegate.class.getMethod("mapReplaceIfPresent", String.class, Object.class, Object.class, Object.class));
      methods.put(MAP_CLEAR, RpcServerObjDelegate.class.getMethod("mapClear", String.class));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public RpcServerObjDelegate(RpcMapService mapService, RpcMultiMapService multiMapService) {
    this.mapService = mapService;
    this.multiMapService = multiMapService;
  }

  public MethodLookup getMethodLookup() {
    return methods::get;
  }

  //RpcMultiMapService

  @Override
  public boolean multiMapCreate(String name) {
    logTrace(() -> "RpcServerObjDelegate.multiMapCreate name = [" + name + "]");
    return multiMapService.multiMapCreate(name);
  }

  @Override
  public <K, V> void multiMapAdd(String name, K k, V v) {
    logTrace(() -> "RpcServerObjDelegate.multiMapAdd name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    multiMapService.multiMapAdd(name, k, v);
  }

  @Override
  public <K, V> boolean multiMapRemove(String name, K k, V v) {
    logTrace(() -> "RpcServerObjDelegate.multiMapRemove name = [" + name + "], k = [" + k + "], v = [" + v + "]");
    return multiMapService.multiMapRemove(name, k, v);
  }


  //RpcMapService

  @Override
  public <K, V> boolean mapCreate(String name) {
    return mapService.mapCreate(name);
  }

  @Override
  public <K, V> void mapPut(String name, K k, V v) {
    mapService.mapPut(name, k, v);
  }

  @Override
  public <K, V> void mapPutAll(String name, Map<K, V> m) {
    mapService.mapPutAll(name, m);
  }

  @Override
  public <K, V> V mapPutIfAbsent(String name, K k, V v) {
    return mapService.mapPutIfAbsent(name, k, v);
  }

  @Override
  public <K, V> V mapRemove(String name, K k) {
    return mapService.mapRemove(name, k);
  }

  @Override
  public <K, V> boolean mapRemoveIfPresent(String name, K k, V v) {
    return mapService.mapRemoveIfPresent(name, k, v);
  }

  @Override
  public <K, V> V mapReplace(String name, K k, V v) {
    return mapService.mapReplace(name, k, v);
  }

  @Override
  public <K, V> boolean mapReplaceIfPresent(String name, K k, V oldValue, V newValue) {
    return mapService.mapReplaceIfPresent(name, k, oldValue, newValue);
  }

  @Override
  public <K, V> void mapClear(String name) {
    mapService.mapClear(name);
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
