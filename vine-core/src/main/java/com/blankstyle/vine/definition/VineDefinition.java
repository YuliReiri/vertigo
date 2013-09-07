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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.grouping.RoundGrouping;


/**
 * A default vine context implementation.
 *
 * @author Jordan Halterman
 */
public class VineDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final int DEFAULT_QUEUE_SIZE = 1000;

  private static final long DEFAULT_TIMEOUT = 5000;

  private static final long DEFAULT_EXPIRATION = 15000;

  public VineDefinition() {
  }

  public VineDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the vine address.
   */
  public String getAddress() {
    return definition.getString("address");
  }

  /**
   * Sets the vine address.
   *
   * @param address
   *   The vine address.
   */
  public VineDefinition setAddress(String address) {
    definition.putString("address", address);
    return this;
  }

  /**
   * Sets a vine option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called vine definition.
   */
  public VineDefinition setOption(String option, String value) {
    switch (option) {
      case "address":
        return setAddress(value);
      default:
        definition.putString(option, value);
        break;
    }
    return this;
  }

  /**
   * Gets a vine option.
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
   * Gets the maximum vine queue size.
   *
   * @return
   *   The maximum vine queue size.
   */
  public int getMaxQueueSize() {
    return definition.getInteger("queue_size", DEFAULT_QUEUE_SIZE);
  }

  /**
   * Sets the maximum vine queue size.
   *
   * @param queueSize
   *   The maximum vine process queue size.
   * @return
   *   The called vine definition.
   */
  public VineDefinition setMaxQueueSize(int queueSize) {
    definition.putNumber("queue_size", queueSize);
    return this;
  }

  /**
   * Gets the vine message timeout.
   */
  public long getMessageTimeout() {
    return definition.getLong("timeout", DEFAULT_TIMEOUT);
  }

  /**
   * Sets the vine message timeout.
   *
   * @param timeout
   *   The vine message timeout.
   */
  public VineDefinition setMessageTimeout(long timeout) {
    definition.putNumber("timeout", timeout);
    return this;
  }

  /**
   * Gets the message expiration.
   */
  public long getMessageExpiration() {
    return definition.getLong("expiration", DEFAULT_EXPIRATION);
  }

  /**
   * Sets the message expiration.
   *
   * @param expiration
   *   The vine message expiration.
   */
  public VineDefinition setMessageExpiration(long expiration) {
    definition.putNumber("expiration", expiration);
    return this;
  }

  /**
   * Adds a connection to a seed definition.
   */
  private SeedDefinition addDefinition(SeedDefinition definition) {
    // Add the seed definition.
    JsonObject seeds = this.definition.getObject("seeds");
    if (seeds == null) {
      seeds = new JsonObject();
      this.definition.putObject("seeds", seeds);
    }
    if (!seeds.getFieldNames().contains(definition.getName())) {
      seeds.putObject(definition.getName(), definition.serialize());
    }

    // Add the seed connection.
    JsonArray connections = this.definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
      this.definition.putArray("connections", connections);
    }
    if (!connections.contains(definition.getName())) {
      connections.add(definition.getName());
    }
    return definition;
  }

  /**
   * Defines a feeder to a seed.
   *
   * @param definition
   *   A seed definition.
   * @return
   *   The passed seed definition.
   */
  public SeedDefinition feed(SeedDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition feed(String name) {
    return feed(name, null, 1);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition feed(String name, String main) {
    return feed(name, main, 1);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
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
  public SeedDefinition feed(String name, String main, int workers) {
    return addDefinition(new SeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

  /**
   * Creates a seed address.
   */
  protected String createSeedAddress(String vineAddress, String seedName) {
    return String.format("%s.%s", vineAddress, seedName);
  }

  /**
   * Creates an array of worker addresses.
   */
  protected String[] createWorkerAddresses(String seedAddress, int numWorkers) {
    List<String> addresses = new ArrayList<String>();
    for (int i = 0; i < numWorkers; i++) {
      addresses.add(String.format("%s.%d", seedAddress, i+1));
    }
    return addresses.toArray(new String[addresses.size()]);
  }

  /**
   * Returns a vine context representation of the vine.
   *
   * @return
   *   A prepared vine context.
   * @throws MalformedDefinitionException 
   */
  public VineContext createContext() throws MalformedDefinitionException {
    String address = definition.getString("address");
    if (address == null) {
      throw new MalformedDefinitionException("No address specified.");
    }

    JsonObject context = new JsonObject();
    context.putString("address", address);
    context.putObject("definition", definition);

    // First, create all seed contexts and then add connections.
    JsonObject seeds = definition.getObject("seeds");
    Iterator<String> iter = seeds.getFieldNames().iterator();

    // Create seed contexts:
    // {
    //   "name": "seed1",
    //   "workers": [
    //     "foo.seed1.1",
    //     "foo.seed1.2"
    //   ],
    //   "definition": {
    //     ...
    //   }
    // }
    JsonObject seedContexts = new JsonObject();
    while (iter.hasNext()) {
      JsonObject seed = seeds.getObject(iter.next());
      JsonObject seedDefinitions = buildSeedsRecursive(seed);
      Iterator<String> iterSeeds = seedDefinitions.getFieldNames().iterator();
      while (iterSeeds.hasNext()) {
        JsonObject seedDef = seedDefinitions.getObject(iterSeeds.next());
        JsonObject seedContext = new JsonObject();
        String seedName = seedDef.getString("name");
        if (seedName == null) {
          throw new MalformedDefinitionException("No seed name specified.");
        }
        seedContext.putString("name", seedName);
        seedContext.putString("address", createSeedAddress(definition.getString("address"), seedDef.getString("name")));
        seedContext.putObject("definition", seedDef);
        seedContext.putArray("workers", new JsonArray(createWorkerAddresses(seedContext.getString("address"), seedContext.getObject("definition").getInteger("workers"))));
        seedContexts.putObject(seedContext.getString("name"), seedContext);
      }
    }

    // Seed contexts are stored in context.seeds.
    context.putObject("seeds", seedContexts);
    

    JsonArray connections = definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
    }

    JsonObject connectionContexts = new JsonObject();

    // Create an object of connection information:
    // {
    //   "seed1": {
    //     "addresses": [
    //       "foo.seed1.1",
    //       "foo.seed1.2"
    //     ]
    //   }
    // }
    Iterator<Object> iter2 = connections.iterator();
    while (iter2.hasNext()) {
      String name = iter2.next().toString();
      JsonObject seedContext = seedContexts.getObject(name);
      if (seedContext == null) {
        continue;
      }

      JsonObject connection = new JsonObject();
      connection.putString("name", name);

      JsonObject grouping = seedContext.getObject("definition").getObject("grouping");
      if (grouping == null) {
        grouping = new RoundGrouping().serialize();
      }

      connection.putObject("grouping", grouping);
      connection.putArray("addresses", seedContext.getArray("workers").copy());

      connectionContexts.putObject(name, connection);
    }

    // Connection information is stored in context.connections.
    context.putObject("connections", connectionContexts);

    // Now iterate through each seed context and add connection information.
    // This needed to be done *after* those contexts are created because
    // we need to be able to get context information from connecting seeds.
    // {
    //   "seed1": {
    //     "addresses": [
    //       "foo.seed1.1",
    //       "foo.seed1.2"
    //     ],
    //     "grouping": "random"
    //   }
    //   ...
    // }
    Iterator<String> seedNames = seedContexts.getFieldNames().iterator();
    while (seedNames.hasNext()) {
      JsonObject seedContext = seedContexts.getObject(seedNames.next());
      JsonObject seedDef = seedContext.getObject("definition");

      // Iterate through each of the seed's connections.
      JsonObject seedCons = seedDef.getObject("connections");
      JsonObject seedConnectionContexts = new JsonObject();

      if (seedCons != null) {
        Set<String> conKeys = seedCons.getFieldNames();
        Iterator<String> iterCon = conKeys.iterator();
  
        while (iterCon.hasNext()) {
          // Get the seed name and with it a reference to the seed context.
          String name = iterCon.next().toString();
          JsonObject conContext = seedContexts.getObject(name);
          if (conContext == null) {
            continue;
          }
  
          // With the context, we can list all of the worker addresses.
          JsonObject connection = new JsonObject();
          connection.putString("name", name);

          // If the connection doesn't define a grouping, use a round grouping.
          JsonObject grouping = conContext.getObject("grouping");
          if (grouping == null) {
            grouping = new RoundGrouping().serialize();
          }

          connection.putObject("grouping", grouping);
          connection.putArray("addresses", conContext.getArray("workers").copy());
  
          seedConnectionContexts.putObject(name, connection);
        }
      }
      // If the seed does not have any connections then it implicitly connects
      // back to the vine verticle.
      else {
        JsonObject connection = new JsonObject();
        connection.putString("name", address);
        connection.putObject("grouping", new RoundGrouping().serialize());
        connection.putArray("addresses", new JsonArray().add(address));

        seedConnectionContexts.putObject(address, connection);
      }

      // Finally, add the connections to the object.
      seedContext.putObject("connections", seedConnectionContexts);
    }

    return new VineContext(context);
  }

  private JsonObject buildSeedsRecursive(JsonObject seedDefinition) {
    return buildSeedsRecursive(seedDefinition, new JsonObject());
  }

  private JsonObject buildSeedsRecursive(JsonObject seedDefinition, JsonObject seeds) {
    seeds.putObject(seedDefinition.getString("name"), seedDefinition);
    JsonObject connections = seedDefinition.getObject("connections");
    if (connections != null) {
      Iterator<String> iter = connections.getFieldNames().iterator();
      while (iter.hasNext()) {
        buildSeedsRecursive(connections.getObject(iter.next()), seeds);
      }
    }
    return seeds;
  }

}
