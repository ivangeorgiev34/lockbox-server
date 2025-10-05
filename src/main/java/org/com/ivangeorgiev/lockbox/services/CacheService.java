package org.com.ivangeorgiev.lockbox.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;

import java.time.Duration;
import java.util.List;

public class CacheService {
    private static final long CACHE_EXPIRY_SECONDS = 60 * 4;
    private static final Cache<String, List<PasswordDto>> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(CACHE_EXPIRY_SECONDS))
            .build();

    public static List<PasswordDto> get(String key) {
        return cache.getIfPresent(key);
    }

    public static void put(String key, List<PasswordDto> passwords) {
        cache.put(key, passwords);
    }

    public static void invalidate(String key) {
        cache.invalidate(key);
    }
}
