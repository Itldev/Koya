package fr.itldev.koya.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nico
 */
public class DefaultCacheFactory extends org.alfresco.repo.cache.AbstractCacheFactory<Serializable, Object> {

    public DefaultCacheAdapter createCache(String cacheName) {
        CacheBuilder builder = CacheBuilder.newBuilder();
        long timeToLive = Long.parseLong(getProperty(cacheName, "timeToLiveSeconds", "0"));
        long maxItems = Long.parseLong(getProperty(cacheName, "maxItems", "0"));
        long maxIdle = Long.parseLong(getProperty(cacheName, "maxIdleSeconds", "0"));

        if (timeToLive >= 0) {
            builder.expireAfterWrite(timeToLive, TimeUnit.SECONDS);
        }
        if (maxItems >= 0) {
            builder.maximumSize(maxItems);
        }
        if (maxIdle >= 0) {
            builder.expireAfterAccess(maxIdle, TimeUnit.SECONDS);
        }

        return new DefaultCacheAdapter(builder.build());
    }

}
