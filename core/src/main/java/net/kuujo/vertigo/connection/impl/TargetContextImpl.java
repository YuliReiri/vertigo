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
package net.kuujo.vertigo.connection.impl;

import net.kuujo.vertigo.impl.BaseContextImpl;
import net.kuujo.vertigo.connection.TargetContext;
import net.kuujo.vertigo.util.Args;

/**
 * Connection source context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TargetContextImpl extends BaseContextImpl<TargetContext> implements TargetContext {
  private String component;
  private String port;
  private int partition;

  @Override
  public String component() {
    return component;
  }

  @Override
  public String port() {
    return port;
  }

  @Override
  public int partition() {
    return partition;
  }

  /**
   * Target context builder.
   */
  public static class Builder implements TargetContext.Builder {
    private final TargetContextImpl target;

    public Builder() {
      target = new TargetContextImpl();
    }

    public Builder(TargetContextImpl source) {
      this.target = source;
    }

    @Override
    public TargetContext.Builder setId(String id) {
      Args.checkNotNull(id, "id cannot be null");
      target.id = id;
      return this;
    }

    @Override
    public Builder setComponent(String component) {
      target.component = component;
      return this;
    }

    @Override
    public Builder setPort(String port) {
      target.port = port;
      return this;
    }

    @Override
    public Builder setPartition(int partition) {
      target.partition = partition;
      return this;
    }

    @Override
    public TargetContextImpl build() {
      return target;
    }
  }

}