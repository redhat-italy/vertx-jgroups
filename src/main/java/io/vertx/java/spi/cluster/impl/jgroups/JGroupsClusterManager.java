package io.vertx.java.spi.cluster.impl.jgroups;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.*;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ClusteredLockImpl;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ClusteredCounterImpl;
import io.vertx.java.spi.cluster.impl.jgroups.listeners.TopologyListener;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.JChannel;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JGroupsClusterManager implements ClusterManager, LambdaLogger {

  private static final Logger LOG = LoggerFactory.getLogger(JGroupsClusterManager.class);

  public static final String CLUSTER_NAME = "JGROUPS_CLUSTER";

  private VertxSPI vertx;

  private CacheManager cacheManager;

  private JChannel channel;

  private CounterService counterService;
  private LockService lockService;

  private volatile boolean active;
  private String address;
  private TopologyListener topologyListener;

  @Override
  public void setVertx(VertxSPI vertx) {
    this.vertx = vertx;
  }

  @Override
  public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Create new AsyncMultiMap [%s] on address [%s]", name, address));
    }
    checkCluster(handler);
    cacheManager.createAsyncMultiMap(name, handler);
  }

  @Override
  public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Create new AsyncMap [%s] on address [%s]", name, address));
    }
    checkCluster(handler);
    cacheManager.createAsyncMap(name, handler);
  }

  @Override
  public <K, V> Map<K, V> getSyncMap(String name) {
    return cacheManager.createSyncMap(name);
  }

  @Override
  public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> handler) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Create new Lock [%s] on address [%s]", name, address));
    }
    checkCluster(handler);
    vertx.executeBlocking(
        () -> {
          ClusteredLockImpl lock = new ClusteredLockImpl(lockService, name);
          if (lock.acquire(timeout)) {
            logDebug(() -> String.format("Lock acquired on [%s]", name));
            return lock;
          } else {
            logError(() -> String.format("Timed out waiting to get lock [%s]", name));
            throw new VertxException(String.format("Timed out waiting to get lock [%s]", name));
          }
        },
        handler
    );
  }

  @Override
  public void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Create new counter [%s] on address [%s]", name, address));
    }
    checkCluster(handler);
    vertx.executeBlocking(
        () -> new ClusteredCounterImpl(vertx, counterService.getOrCreateCounter(name, 0L)),
        handler
    );
  }

  @Override
  public String getNodeID() {
    return address;
  }

  @Override
  public List<String> getNodes() {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("GetNodes on address [%s] with channel view [%s]", address, channel.getViewAsString()));
    }
    return topologyListener.getNodes();
  }

  @Override
  public void nodeListener(NodeListener listener) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Set nodeListener [%s] on address [%s]", listener, address));
    }
    topologyListener.setNodeListener(listener);
  }

  @Override
  public void join(Handler<AsyncResult<Void>> handler) {
    vertx.executeBlocking(() -> {
      if (active) {
        return null;
      }
      active = true;

      try {
        channel = new JChannel("jgroups-udp.xml");
        topologyListener = new TopologyListener(vertx);
        channel.setReceiver(topologyListener);
        channel.connect(CLUSTER_NAME);

        address = channel.getAddressAsString();

        if(LOG.isInfoEnabled()) {
          LOG.info(String.format("Node id [%s] join the cluster", this.getNodeID()));
        }

        counterService = new CounterService(channel);
        lockService = new LockService(channel);

        cacheManager = new CacheManager(vertx, channel);
        cacheManager.start();

        return null;
      } catch (Exception e) {
        active = false;
        throw new RuntimeException(e);
      }
    }, handler);
  }

  @Override
  public void leave(Handler<AsyncResult<Void>> handler) {
    vertx.executeBlocking(() -> {
      if (!active) {
        return null;
      }
      active = false;

      if(LOG.isInfoEnabled()) {
        LOG.info(String.format("Node id [%s] leave the cluster", this.getNodeID()));
      }

      channel.close();

      channel = null;
      address = null;

      return null;
    }, handler);
  }

  @Override
  public boolean isActive() {
    return active;
  }

  private <R> void checkCluster(Handler<AsyncResult<R>> handler) {
    if (!active) {
      throw new VertxException("Cluster is not active!");
    }
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
