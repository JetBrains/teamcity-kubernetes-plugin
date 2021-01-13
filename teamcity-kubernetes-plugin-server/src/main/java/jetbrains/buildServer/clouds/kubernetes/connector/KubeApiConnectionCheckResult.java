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

package jetbrains.buildServer.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public class KubeApiConnectionCheckResult {
    private final String myMessage;
    private final boolean mySuccess;
    private final boolean myNeedRefresh;

    private KubeApiConnectionCheckResult(String message, boolean success, boolean needRefresh) {
        myMessage = message;
        mySuccess = success;
        myNeedRefresh = needRefresh;
    }

    public static KubeApiConnectionCheckResult ok(String message) {
        return new KubeApiConnectionCheckResult(message, true, false);
    }

    public static KubeApiConnectionCheckResult error(String message, boolean needRefresh) {
        return new KubeApiConnectionCheckResult(message, false, needRefresh);
    }

    public String getMessage() {
        return myMessage;
    }

    public boolean isSuccess() {
        return mySuccess;
    }

    public boolean isNeedRefresh() {
        return myNeedRefresh;
    }

    @Override
    public String toString() {
        if (myNeedRefresh)
            return "Need refresh: " + myMessage;

        return myMessage;
    }
}
