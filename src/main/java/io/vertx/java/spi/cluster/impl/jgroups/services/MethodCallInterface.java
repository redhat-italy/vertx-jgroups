package io.vertx.java.spi.cluster.impl.jgroups.services;

import org.jgroups.blocks.MethodCall;

public interface MethodCallInterface {

  public interface OneParameter extends MethodCallInterface {
    MethodCall method(String name);
  }

  public interface TwoParameters extends MethodCallInterface {
    MethodCall method(String name, Object p1);
  }

  public interface ThreeParameters extends MethodCallInterface {
    MethodCall method(String name, Object p1, Object p2);
  }

  public interface FourParameters extends MethodCallInterface {
    MethodCall method(String name, Object p1, Object p2, Object p3);
  }
}
