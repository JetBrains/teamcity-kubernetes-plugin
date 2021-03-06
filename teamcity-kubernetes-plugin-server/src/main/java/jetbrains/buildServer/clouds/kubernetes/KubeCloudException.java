/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import jetbrains.buildServer.clouds.CloudException;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class KubeCloudException extends CloudException {
    public KubeCloudException(String message) {
        super(message);
    }

    public KubeCloudException(String message, Throwable cause) {
        super(message, cause);
    }
}
