package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.config.RedisCacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheInvalidationService {
    private final CacheManager cacheManager;

    public CacheInvalidationService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearMoneyCaches() {
        clearCaches(List.of(RedisCacheConfig.DASHBOARD_CACHE, RedisCacheConfig.MONEY_PLAN_CACHE));
    }

    public void clearCategoryCaches() {
        clearCaches(List.of(
                RedisCacheConfig.CATEGORIES_CACHE,
                RedisCacheConfig.DASHBOARD_CACHE,
                RedisCacheConfig.MONEY_PLAN_CACHE
        ));
    }

    private void clearCaches(List<String> cacheNames) {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
