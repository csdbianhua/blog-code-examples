
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;


public class ThreadCacheManagerImpl implements ThreadCacheManager {

    private ThreadCache threadCache;

    public ThreadCacheManagerImpl() {
        threadCache = new ThreadCache(true);
    }

    @Override
    public void clear() {
        threadCache.clear();
    }

    @Override
    public Cache getCache(String name) {
        return threadCache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.singletonList(THREAD);
    }

    public class ThreadCache extends AbstractValueAdaptingCache {

        private final ThreadLocal<Map<Object, Object>> store = ThreadLocal.withInitial(() -> new HashMap<>(64));


        protected ThreadCache(boolean allowNullValues) {
            super(allowNullValues);
        }

        @Override
        protected Object lookup(Object key) {
            return store.get().get(key);
        }

        @Override
        public String getName() {
            return "thread";
        }

        @Override
        public Object getNativeCache() {
            return store.get();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Callable<T> valueLoader) {
            return (T) fromStoreValue(this.store.get().computeIfAbsent(key, r -> {
                try {
                    return toStoreValue(valueLoader.call());
                } catch (Throwable ex) {
                    throw new ValueRetrievalException(key, valueLoader, ex);
                }
            }));
        }

        @Override
        public void put(Object key, Object value) {
            this.store.get().put(key, toStoreValue(value));
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            Object existing = this.store.get().putIfAbsent(key, toStoreValue(value));
            return toValueWrapper(existing);
        }

        @Override
        public void evict(Object key) {
            this.store.get().remove(key);
        }

        @Override
        public void clear() {
            this.store.get().clear();
        }
    }
}
