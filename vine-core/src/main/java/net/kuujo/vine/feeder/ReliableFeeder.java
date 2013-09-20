/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vine.feeder;

import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A reliable feeder implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableFeeder extends UnreliableFeeder {

  private Handler<Void> drainHandler;

  private long feedQueueSize = 10000;

  private long inProcess;

  private boolean queueFull;

  public ReliableFeeder(VineContext context, Vertx vertx) {
    super(context, vertx);
  }

  public ReliableFeeder(VineContext context, EventBus eventBus) {
    super(context, eventBus);
  }

  public ReliableFeeder(VineContext context, Vertx vertx, EventBus eventBus) {
    super(context, vertx, eventBus);
  }

  @Override
  public boolean feedQueueFull() {
    return queueFull;
  }

  @Override
  public Feeder feed(final JsonObject data) {
    inProcess++;
    checkQueue();
    output.emit(new JsonMessage(data), new Handler<Boolean>() {
      @Override
      public void handle(Boolean succeeded) {
        if (!succeeded) {
          feed(data);
        }
        else {
          inProcess--;
          checkQueue();
        }
      }
    });
    return this;
  }

  @Override
  public Feeder feed(final JsonObject data, final Handler<AsyncResult<Void>> doneHandler) {
    inProcess++;
    checkQueue();
    output.emit(new JsonMessage(data), new Handler<Boolean>() {
      @Override
      public void handle(Boolean succeeded) {
        if (!succeeded) {
          inProcess--;
          checkQueue();
          feed(data, doneHandler);
        }
        else {
          new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
        }
      }
    });
    return this;
  }

  @Override
  public Feeder drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Checks the feed queue status and pauses or unpauses the feeder
   * as necessary.
   */
  private void checkQueue() {
    if (!queueFull) {
      if (inProcess >= feedQueueSize) {
        pause();
      }
    }
    else {
      if (inProcess < feedQueueSize / 2) {
        unpause();
      }
    }
  }

  /**
   * Pauses the feeder.
   */
  private void pause() {
    queueFull = true;
  }

  /**
   * Unpauses the feeder.
   */
  private void unpause() {
    queueFull = false;
    if (drainHandler != null) {
      drainHandler.handle(null);
    }
  }

}
