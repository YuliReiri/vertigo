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
package com.blankstyle.vine.seed;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.Handler;

import com.blankstyle.vine.messaging.JsonMessage;

/**
 * A reliable seed implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableSeed extends BasicSeed {

  private Map<JsonMessage, ReliableTree> messageTrees = new HashMap<>();

  /**
   * Represents a reliable parent-children relationship.
   */
  private class ReliableTree {
    private JsonMessage parent;
    private Set<JsonMessage> processing = new HashSet<>();
    private Set<JsonMessage> finished = new HashSet<>();
    private boolean complete;

    public ReliableTree(JsonMessage parent) {
      this.parent = parent;
    }

    /**
     * Emits a new tree child.
     *
     * For each output stream, a copy of the child is created. This allows each
     * stream to ack the message separately, ensuring that one ack does not
     * improperly result in a "completed" status in seeds that contain more than
     * one output stream.
     *
     * @param child
     *   The child message.
     */
    public void emit(JsonMessage child) {
      // Ensure the child's ID is the same as the parent's (if any).
      child.setIdentifier(parent.getIdentifier());
      for (String name : output.getStreamNames()) {
        doEmit(name, child.copy());
      }
    }

    /**
     * Emits multiple tree children.
     *
     * All children are added to tracking prior to any messages being emitted
     * via the output collector. This ensures that messages are not acked before
     * all messages have been stored.
     *
     * @param children
     *   The child messages.
     */
    public void emit(Collection<JsonMessage> children) {
      Set<JsonMessage> copies = new HashSet<JsonMessage>();
      for (JsonMessage child : children) {
        child.setIdentifier(parent.getIdentifier());
        JsonMessage copy = child.copy();
        processing.add(copy);
        copies.add(copy);
      }

      Set<String> streamNames = output.getStreamNames();
      for (JsonMessage copy : copies) {
        for (String streamName : streamNames) {
          doEmit(streamName, copy);
        }
      }
    }

    /**
     * Emits a new tree child to a specific output stream.
     *
     * @param name
     *   The output stream to which to emit the child.
     * @param child
     *   The child message.
     */
    public void emitTo(String name, JsonMessage child) {
      child.setIdentifier(parent.getIdentifier());
      doEmit(name, child.copy());
    }

    /**
     * Emits multiple tree children to a specific output stream.
     *
     * All children are added to tracking prior to any messages being emitted
     * via the output collector. This ensures that messages are not acked before
     * all messages have been stored.
     *
     * @param name
     *   The output stream to which to emit the children.
     * @param children
     *   The child messages.
     */
    public void emitTo(String name, Collection<JsonMessage> children) {
      Set<JsonMessage> copies = new HashSet<JsonMessage>();
      for (JsonMessage child : children) {
        child.setIdentifier(parent.getIdentifier());
        JsonMessage copy = child.copy();
        processing.add(copy);
        copies.add(copy);
      }

      for (JsonMessage copy : copies) {
        doEmit(name, copy);
      }
    }

    /**
     * Emits a child message to a specific stream by name.
     */
    private void doEmit(final String name, final JsonMessage child) {
      // This method is called recursively, so check that the child has not
      // already been added to processing.
      if (!processing.contains(child)) {
        processing.add(child);
      }

      output.emitTo(name, child, new Handler<Boolean>() {
        @Override
        public void handle(Boolean succeeded) {
          if (succeeded) {
            if (processing.contains(child)) {
              processing.remove(child);
            }
            if (!finished.contains(child)) {
              finished.add(child);
            }

            // If the tree contains no more messages in processing, ack the parent.
            if (processing.size() == 0 && !complete) {
              parent.ready();
              complete = true;
            }
          }
          else {
            doEmit(name, child);
          }
        }
      });
    }
  }

  /**
   * Returns the message tree associated with a specific parent. If no message
   * tree yet exists, one will be created.
   */
  private ReliableTree getTree(JsonMessage parent) {
    ReliableTree tree;
    if (!messageTrees.containsKey(parent)) {
      tree = new ReliableTree(parent);
      messageTrees.put(parent, tree);
    }
    else {
      tree = messageTrees.get(parent);
    }
    return tree;
  }

  @Override
  public void emit(JsonMessage message, JsonMessage parent) {
    getTree(parent).emit(message);;
  }

  @Override
  public void emit(Collection<JsonMessage> messages, JsonMessage parent) {
    getTree(parent).emit(messages);
  }

  @Override
  public void emitTo(String seedName, JsonMessage message, JsonMessage parent) {
    getTree(parent).emitTo(seedName, message);
  }

  @Override
  public void emitTo(String seedName, Collection<JsonMessage> messages, JsonMessage parent) {
    getTree(parent).emitTo(seedName, messages);
  }

}
