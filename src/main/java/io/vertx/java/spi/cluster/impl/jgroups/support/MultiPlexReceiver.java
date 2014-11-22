package io.vertx.java.spi.cluster.impl.jgroups.support;

import org.jgroups.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class MultiPlexReceiver implements Receiver {

  private final static Set<Receiver> receivers = new HashSet<>();

  @Override
  public void viewAccepted(View newView) {
    receivers.parallelStream().forEach((r) -> r.viewAccepted(newView));
  }

  @Override
  public void suspect(Address suspected_mbr) {
    receivers.parallelStream().forEach((r) -> r.suspect(suspected_mbr));
  }

  @Override
  public void block() {
    receivers.parallelStream().forEach((r) -> r.block());
  }

  @Override
  public void unblock() {
    receivers.parallelStream().forEach((r) -> r.unblock());
  }

  @Override
  public void receive(Message msg) {
    receivers.parallelStream().forEach((r) -> r.receive(msg));
  }

  @Override
  public void getState(OutputStream output) throws Exception {
    for (Receiver r : receivers) {
      r.getState(output);
    }
  }

  @Override
  public void setState(InputStream input) throws Exception {
    for (Receiver r : receivers) {
      r.setState(input);
    }
  }

  public void addReceiver(Receiver receiver) {
    receivers.add(receiver);
  }
}
