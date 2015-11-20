package fr.itldev.koya.services.impl.util;

import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class CacheConfig implements Serializable {

    private static final Logger logger = Logger.getLogger(CacheConfig.class);

    private CacheConfig() {

    }

    private Boolean enabled;
    private Integer maxSize;
    private Integer expireAfterWriteSeconds;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getExpireAfterWriteSeconds() {
        return expireAfterWriteSeconds;
    }

    public void setExpireAfterWriteSeconds(Integer expireAfterWriteSeconds) {
        this.expireAfterWriteSeconds = expireAfterWriteSeconds;
    }

    public static CacheConfig noCache() {
        CacheConfig instance = new CacheConfig();
        instance.setEnabled(Boolean.FALSE);
        return instance;
    }

    public void debugLogConfig(String cacheName) {
        logger.debug("Cache : " + cacheName + " " + (enabled ? "Active" : "Disabled"));
        if (enabled) {
            logger.debug(" * maxSize=" + maxSize);
            logger.debug(" * expireAfterWrite=" + expireAfterWriteSeconds + " seconds");
        }
    }

}
