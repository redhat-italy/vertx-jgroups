package io.vertx.java.spi.cluster.impl.jgroups.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.support.DataHolder;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.Message;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultRpcExecutorService implements RpcExecutorService, LambdaLogger {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultRpcExecutorService.class);
  private static final RequestOptions REQUEST_OPTIONS_BLOCKING = new RequestOptions().setFlags(Message.Flag.NO_TOTAL_ORDER).setMode(ResponseMode.GET_ALL);

  private final VertxSPI vertx;
  private final RpcDispatcher dispatcher;

  public DefaultRpcExecutorService(VertxSPI vertx, RpcDispatcher dispatcher) {
    this.vertx = vertx;
    this.dispatcher = dispatcher;
  }

  @Override
  public <T> T remoteExecute(MethodCall action) {
    return this.execute(action);
  }

  @Override
  public <T> void remoteExecute(MethodCall action, Handler<AsyncResult<T>> handler) {
    logTrace(() -> String.format("RemoteExecute action %s, handler %s", action, handler));
    this.<T>asyncExecute(() -> this.<T>execute(action), handler);
  }

  @Override
  public <T> void asyncExecute(Supplier<T> action, Handler<AsyncResult<T>> handler) {
    logTrace(() -> String.format("AsyncExecute action %s, handler %s", action.toString(), handler.toString()));
    vertx.executeBlocking(action::get, handler);
  }

  private <T> T execute(MethodCall action) {
    logTrace(() -> String.format("Execute action [%s]", action.toStringDetails()));
    RspList<Object> responses;
    try {
      responses = this.<Object>broadDispatch(action, REQUEST_OPTIONS_BLOCKING);
    } catch (Exception e) {
      throw new VertxException(e);
    }

    logTrace(() -> {
      String values = responses.values().stream()
          .map(Rsp::toString)
          .collect(Collectors.joining(", ", "[", "]"));

      return String.format("Response from method execution %s", values);
    });

    Optional<Rsp<Object>> optional = responses.values().stream()
        .filter(Rsp::hasException)
        .findFirst();
    if (optional.isPresent()) {
      throw new VertxException(optional.get().getException());
    }
    Object response = responses.getFirst();
    if (DataHolder.class.isInstance(response)) {
      return ((DataHolder<T>) response).unwrap();
    } else {
      return (T) response;
    }
  }

  private <T> RspList<T> broadDispatch(MethodCall action, RequestOptions options) throws Exception {
    logTrace(() -> String.format("BroadDispatch action [%s] - options [%s]", action.toStringDetails(), options.toString()));
    return dispatcher.callRemoteMethods(null, action, options);
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
