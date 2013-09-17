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
package com.blankstyle.vine.definition;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;


/**
 * A default seed context implementation.
 *
 * @author Jordan Halterman
 */
public class SeedDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final int DEFAULT_NUM_WORKERS = 1;

  private static final long DEFAULT_HEARTBEAT_INTERVAL = 1000;

  public SeedDefinition() {
  }

  public SeedDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the seed name.
   */
  public String getName() {
    return definition.getString("name");
  }

  /**
   * Sets the seed name.
   *
   * @param name
   *   The seed name.
   */
  public SeedDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  /**
   * Gets the seed main.
   */
  public String getMain() {
    return definition.getString("main");
  }

  /**
   * Sets the seed main. This is a string reference to the verticle
   * to be run when a seed worker is started.
   *
   * @param main
   *   The seed main.
   */
  public SeedDefinition setMain(String main) {
    definition.putString("main", main);
    return this;
  }

  /**
   * Sets a seed option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called seed definition.
   */
  public SeedDefinition setOption(String option, String value) {
    switch (option) {
      case "name":
        return setName(value);
      case "main":
        return setMain(value);
      default:
        definition.putString(option, value);
        break;
    }
    return this;
  }

  /**
   * Gets a seed option.
   *
   * @param option
   *   The option to get.
   * @return
   *   The option value.
   */
  public String getOption(String option) {
    return definition.getString(option);
  }

  /**
   * Sets the seed worker grouping.
   *
   * @param grouping
   *   A grouping definition.
   * @return
   *   The called seed definition.
   */
  public SeedDefinition groupBy(GroupingDefinition grouping) {
    definition.putObject("grouping", grouping.serialize());
    return this;
  }

  /**
   * Gets the seed worker grouping.
   */
  public GroupingDefinition getGrouping() {
    return new GroupingDefinition(definition.getObject("grouping"));
  }

  /**
   * Sets the number of seed workers.
   *
   * @param workers
   *   The number of seed workers.
   * @return
   *   The called seed definition.
   */
  public SeedDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

  /**
   * Gets the number of seed workers.
   */
  public int getWorkers() {
    return definition.getInteger("workers", DEFAULT_NUM_WORKERS);
  }

  /**
   * Sets the seed worker heartbeat interval.
   *
   * @param interval
   *   A heartbeat interval.
   * @return
   *   The called seed definition.
   */
  public SeedDefinition setHeartbeatInterval(long interval) {
    definition.putNumber("heartbeat", interval);
    return this;
  }

  /**
   * Gets the seed heartbeat interval.
   *
   * @return
   *   A seed heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return definition.getLong("heartbeat", DEFAULT_HEARTBEAT_INTERVAL);
  }

  /**
   * Adds a connection to a seed definition.
   */
  private SeedDefinition addDefinition(SeedDefinition definition) {
    JsonObject connections = this.definition.getObject("connections");
    if (connections == null) {
      connections = new JsonObject();
      this.definition.putObject("connections", connections);
    }
    if (!connections.getFieldNames().contains(definition.getName())) {
      connections.putObject(definition.getName(), definition.serialize());
    }
    return definition;
  }

  /**
   * Creates a connection to the given definition.
   *
   * @param definition
   *   A seed definition.
   */
  public SeedDefinition to(SeedDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Creates a connection to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition to(String name) {
    return to(name, null, 1);
  }

  /**
   * Creates a connection to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition to(String name, String main) {
    return to(name, main, 1);
  }

  /**
   * Creates a connection to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition to(String name, String main, int workers) {
    return addDefinition(new SeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
