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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A message dispatcher.
 *
 * @author Jordan Halterman
 */
public interface Dispatcher {

  /**
   * Initializes the dispatcher.
   *
   * @param connections
   *   A collection of connections to which the dispatcher will dispatch messages.
   */
  public void init(ConnectionPool connections);

  /**
   * Sets a dispatcher option.
   *
   * @param option
   *   The option name.
   * @param value
   *   The option value.
   * @return
   *   The called dispatcher instance.
   */
  public Dispatcher setOption(String option, String value);

  /**
   * Gets a dispatcher option.
   *
   * @param option
   *   The option name.
   * @return
   *   The option value.
   */
  public String getOption(String option);

  /**
   * Gets a dispatcher option.
   *
   * @param option
   *   The option name.
   * @param defaultValue
   *   A default value for the option.
   * @return
   *   The option value.
   */
  public String getOption(String option, String defaultValue);

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   */
  public void dispatch(JsonMessage message);

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   * @param resultHandler
   *   A result handler that will be invoked when the message is acknowledged.
   */
  public void dispatch(JsonMessage message, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   * @param timeout
   *   A message timeout.
   * @param resultHandler
   *   A result handler that will be invoked when the message is acknowledged.
   */
  public void dispatch(JsonMessage message, long timeout, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   * @param timeout
   *   A message timeout.
   * @param retry
   *   A boolean indicating whether to retry dispatching the message if a timeout occurs.
   * @param resultHandler
   *   A result handler that will be invoked when the message is acknowledged.
   */
  public void dispatch(JsonMessage message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Dispatches a message.
   *
   * @param message
   *   The message to dispatch.
   * @param timeout
   *   A message timeout.
   * @param retry
   *   A boolean indicating whether to retry dispatching the message if a timeout occurs.
   * @param attempts
   *   The maximum number of retry attempts allows before an exception is thrown.
   * @param resultHandler
   *   A result handler that will be invoked when the message is acknowledged.
   */
  public void dispatch(JsonMessage message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Void>> resultHandler);

}
