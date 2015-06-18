package fr.itldev.koya.cache;

import com.google.common.cache.Cache;
import java.io.Serializable;
import java.util.Collection;
import org.alfresco.repo.cache.SimpleCache;

/**
 *
 * @author nico
 */
public class DefaultCacheAdapter<K extends Serializable, V> implements SimpleCache<K, V> {

    Cache<K, V> cache;

    public DefaultCacheAdapter(Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public boolean contains(K key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public Collection<K> getKeys() {
        return cache.asMap().keySet();
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

}
