
import org.springframework.cache.CacheManager;

public interface ThreadCacheManager extends CacheManager {
    /**
     * 清理当前线程所有的缓存
     */
    void clear();

}
