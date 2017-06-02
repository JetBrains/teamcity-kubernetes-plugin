package jetbrains.buildServer.controllers.admin.projects;

import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

import javax.servlet.http.HttpServletRequest;

//NOTE: copy pasted from jetbrains.buildServer.controllers.admin.projects.PluginPropertiesUtil
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
