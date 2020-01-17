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

package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.InstanceStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 24.10.17.
 */
public interface KubeDataCache {
    @NotNull
    Date getInstanceStartedTime(@NotNull String instanceId, @NotNull Callable<Date> resolver) throws Exception;

    @NotNull
    InstanceStatus getInstanceStatus(@NotNull String instanceId, @NotNull Callable<InstanceStatus> resolver) throws Exception;

    void cleanInstanceStatus(@NotNull String instanceId);

    void invalidate();
}
