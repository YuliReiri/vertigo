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
package net.kuujo.vevent.messaging;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

/**
 * An abstract dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractDispatcher implements Dispatcher {

  private Map<String, String> options = new HashMap<String, String>();

  @Override
  public Dispatcher setOption(String option, String value) {
    options.put(option, value);
    return this;
  }

  @Override
  public String getOption(String option) {
    return options.get(option);
  }

  @Override
  public String getOption(String option, String defaultValue) {
    if (options.containsKey(option)) {
      return options.get(option);
    }
    return defaultValue;
  }

  /**
   * Returns the next connection to which to dispatch.
   *
   * @param message
   *   The message for which a connection is being retrieved.
   * @return
   *   A connection to which to dispatch a message.
   */
  protected abstract Connection getConnection(JsonMessage message);

  @Override
  public void dispatch(JsonMessage message) {
    getConnection(message).send(message);
  }

  @Override
  public <T> void dispatch(JsonMessage message, Handler<AsyncResult<Message<T>>> replyHandler) {
    getConnection(message).send(message, replyHandler);
  }

  @Override
  public <T> void dispatch(JsonMessage message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    getConnection(message).send(message, timeout, replyHandler);
  }

  @Override
  public <T> void dispatch(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Message<T>>> replyHandler) {
    getConnection(message).send(message, timeout, retry, replyHandler);
  }

  @Override
  public <T> void dispatch(JsonMessage message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Message<T>>> replyHandler) {
    getConnection(message).send(message, timeout, retry, attempts, replyHandler);
  }

}