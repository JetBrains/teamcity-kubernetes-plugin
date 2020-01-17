/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeKubeApiConnector implements KubeApiConnector {


  public FakeKubeApiConnector(){}

  @NotNull
  @Override
  public KubeApiConnectionCheckResult testConnection() {
    return null;
  }

  @NotNull
  @Override
  public Pod createPod(@NotNull final Pod podTemplate) {
    return null;
  }

  @Override
  public boolean deletePod(@NotNull final Pod pod, final long gracePeriod) {
    return false;
  }

  @NotNull
  @Override
  public Collection<Pod> listPods(@NotNull final Map<String, String> labels) {
    return null;
  }

  @Nullable
  @Override
  public Deployment getDeployment(@NotNull final String deploymentName) {
    return null;
  }

  @Nullable
  @Override
  public PodStatus getPodStatus(@NotNull final String podName) {
    return null;
  }

  @NotNull
  @Override
  public Collection<String> listNamespaces() {
    return null;
  }

  @NotNull
  @Override
  public Collection<String> listDeployments() {
    return null;
  }

  @Override
  public void invalidate() {

  }
}
