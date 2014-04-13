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
package net.kuujo.vertigo.input;

import org.vertx.java.core.Handler;

/**
 * Input groups allow incoming messages to be separated into groups. Groups are
 * named structures of messages that are defined by the component outputting messages.
 *
 * @author Jordan Halterman
 */
public interface InputGroup extends Input<InputGroup> {

  /**
   * Returns the input group name.
   *
   * @return The group name.
   */
  String name();

  /**
   * Registers a start handler on the group.
   *
   * @param handler A handler to be called when the group is started.
   * @return The input group.
   */
  InputGroup startHandler(Handler<Void> handler);

  /**
   * Registers an end handler on the group.
   *
   * @param handler A handler to be called when the group is ended.
   * @return The input group.
   */
  InputGroup endHandler(Handler<Void> handler);

}
