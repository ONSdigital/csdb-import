package com.github.onsdigital.csdbimport.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class to get configuration values from {@link System#getProperty(String)} or gracefully fall back to {@link System#getenv()}.
 */
public class Configuration {

    public static final String SCP_ROOT_DIR = System.getProperty("user.dir");
    public static final String SCP_AUTHORIZED_KEYS = System.getProperty("user.home") + "/.ssh/authorized_keys";

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank.
     *
     * @param key The configuration value key.
     * @return A system property or, if that comes back blank, an environment value.
     */
    public static String get(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank, then falling back to the default value.
     *
     * @param key          The configuration value key.
     * @param defaultValue The default to use if neither a property nor an environment value are present.
     * @return The result of {@link #get(String)}, or <code>defaultValue</code> if that result is blank.
     */
    public static String get(String key, String defaultValue) {
        return get(StringUtils.defaultIfBlank(get(key), defaultValue));
    }

}
