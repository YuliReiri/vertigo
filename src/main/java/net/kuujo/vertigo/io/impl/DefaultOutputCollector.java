/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.io.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.context.impl.DefaultOutputContext;
import net.kuujo.vertigo.context.impl.DefaultOutputPortContext;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPort;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector, Observer<OutputContext> {
  private final Vertx vertx;
  private OutputContext context;
  private final Map<String, OutputPort> ports = new HashMap<>();
  private boolean started;

  public DefaultOutputCollector(Vertx vertx) {
    this.vertx = vertx;
  }

  public DefaultOutputCollector(Vertx vertx, OutputContext context) {
    this.vertx = vertx;
    this.context = context;
    context.registerObserver(this);
  }

  @Override
  public OutputContext context() {
    return context;
  }

  @Override
  public Collection<OutputPort> ports() {
    return ports.values();
  }

  @Override
  public OutputPort port(String name) {
    OutputPort port = ports.get(name);
    if (port == null) {
      OutputPortContext context = DefaultOutputPortContext.Builder.newBuilder()
          .setAddress(UUID.randomUUID().toString())
          .setName(name)
          .build();
      DefaultOutputContext.Builder.newBuilder((DefaultOutputContext) this.context).addPort((DefaultOutputPortContext) context);
      port = new DefaultOutputPort(vertx, context);
      ports.put(name, port);
    }
    return port;
  }

  @Override
  public void update(OutputContext update) {
    for (OutputPortContext output : update.ports()) {
      boolean exists = false;
      for (OutputPort port : ports.values()) {
        if (port.context().equals(output)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        OutputPort port = new DefaultOutputPort(vertx, output);
        if (started) {
          port.open();
        }
        ports.put(output.name(), port);
      }
    }
  }

  @Override
  public OutputCollector open() {
    return open(null);
  }

  @Override
  public OutputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    if (!started) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.ports().size());
      startCounter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });

      for (OutputPortContext port : context.ports()) {
        if (ports.containsKey(port.name())) {
          ((DefaultOutputPort) ports.get(port.name())).open(startCounter);
        } else {
          ports.put(port.name(), new DefaultOutputPort(vertx, port).open(startCounter));
        }
      }
      started = true;
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    if (started) {
      final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(ports.size());
      stopCounter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            ports.clear();
            started = false;
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });
  
      for (OutputPort output : ports.values()) {
        output.close(stopCounter);
      }
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
  }

}