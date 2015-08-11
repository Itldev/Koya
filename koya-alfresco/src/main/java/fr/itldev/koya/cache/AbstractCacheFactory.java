package fr.itldev.koya.cache;

import com.google.common.cache.Cache;
import java.util.Properties;
import org.alfresco.repo.cache.DefaultCacheFactory;

/**
 *
 * @author nico
 */
public abstract class AbstractCacheFactory {

    private static final String PROP_SEPERATOR = ".";
    private Properties properties;

    public String getProperty(String cacheName, String propName,
            final String defaultValue) {
        final String fqPropName = cacheName + PROP_SEPERATOR + propName;
        String value = properties.getProperty(fqPropName);
        if (value != null) {
            value = value.trim();
        }
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Provide properties to parameterize cache creation. Cache properties are
     * prefixed with the cacheName supplied when invoking
     * {@link DefaultCacheFactory#createCache(String)}. For example, for a cache
     * named cache.ticketsCache the property cache.ticketsCache.maxItems will
     * determine the capacity of the cache.
     *
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public abstract Cache createCache(String cacheName);

}
