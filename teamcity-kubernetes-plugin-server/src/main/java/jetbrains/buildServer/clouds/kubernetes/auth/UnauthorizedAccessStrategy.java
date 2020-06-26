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

package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.util.Collection;
import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class UnauthorizedAccessStrategy implements KubeAuthStrategy {

    public static final String ID = "unauthorized";

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unauthorized Access";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Use unauthorized access to the Kubernetes API server";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        return clientConfig;
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        return null;
    }
}
