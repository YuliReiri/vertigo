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
package net.kuujo.vertigo.cluster;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.cluster.impl.DefaultClusterContext;

/**
 * Cluster context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultClusterContext.class
)
public interface ClusterContext extends Context<ClusterContext> {

  /**
   * Returns the cluster address.
   *
   * @return The cluster address.
   */
  String address();

  /**
   * Returns the cluster scope.
   *
   * @return The cluster scope.
   */
  ClusterScope scope();

}