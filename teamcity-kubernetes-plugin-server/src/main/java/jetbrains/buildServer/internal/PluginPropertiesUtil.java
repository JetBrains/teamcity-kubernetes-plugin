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

package jetbrains.buildServer.internal;

import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

import javax.servlet.http.HttpServletRequest;

//NOTE: copy pasted from PluginPropertiesUtil
public class PluginPropertiesUtil {
    private final static String PROPERTY_PREFIX = "prop:";
    private static final String ENCRYPTED_PROPERTY_PREFIX = "prop:encrypted:";

    private PluginPropertiesUtil() {}

    public static void bindPropertiesFromRequest(HttpServletRequest request, BasePropertiesBean bean) {
        bindPropertiesFromRequest(request, bean, false);
    }

    public static void bindPropertiesFromRequest(HttpServletRequest request, BasePropertiesBean bean, boolean includeEmptyValues) {
        bean.clearProperties();

        for (final Object o : request.getParameterMap().keySet()) {
            String paramName = (String)o;
            if (paramName.startsWith(PROPERTY_PREFIX)) {
                if (paramName.startsWith(ENCRYPTED_PROPERTY_PREFIX)) {
                    setEncryptedProperty(paramName, request, bean, includeEmptyValues);
                } else {
                    setStringProperty(paramName, request, bean, includeEmptyValues);
                }
            }
        }
    }

    private static void setStringProperty(final String paramName, final HttpServletRequest request,
                                          final BasePropertiesBean bean, final boolean includeEmptyValues) {
        String propName = paramName.substring(PROPERTY_PREFIX.length());
        final String propertyValue = request.getParameter(paramName).trim();
        if (includeEmptyValues || propertyValue.length() > 0) {
            bean.setProperty(propName, toUnixLineFeeds(propertyValue));
        }
    }

    private static void setEncryptedProperty(final String paramName, final HttpServletRequest request,
                                             final BasePropertiesBean bean, final boolean includeEmptyValues) {
        String propName = paramName.substring(ENCRYPTED_PROPERTY_PREFIX.length());
        String propertyValue = RSACipher.decryptWebRequestData(request.getParameter(paramName));
        if (propertyValue != null && (includeEmptyValues || propertyValue.length() > 0)) {
            bean.setProperty(propName, toUnixLineFeeds(propertyValue));
        }
    }

    private static String toUnixLineFeeds(final String str) {
        return str.replace("\r", "");
    }
}
