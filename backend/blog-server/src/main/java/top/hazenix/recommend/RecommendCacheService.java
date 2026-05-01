package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheUserRecommendations(Long userId, Map<Long, Double> articleScores) {
        String key = RecommendConstants.KEY_REC_USER + userId;
        cacheZSet(key, articleScores, RecommendConstants.TTL_REC_USER);
    }

    public void cacheAnonymousRecommendations(Map<Long, Double> articleScores) {
        cacheZSet(RecommendConstants.KEY_REC_ANONYMOUS, articleScores, RecommendConstants.TTL_REC_ANONYMOUS);
    }

    public void cacheColdStartRecommendations(Long userId, Map<Long, Double> articleScores) {
        String key = RecommendConstants.KEY_REC_COLD + userId;
        cacheZSet(key, articleScores, RecommendConstants.TTL_REC_USER);
    }

    public List<Long> getUserRecommendations(Long userId, int size) {
        String key = RecommendConstants.KEY_REC_USER + userId;
        return getFromZSet(key, size);
    }

    public List<Long> getAnonymousRecommendations(int size) {
        return getFromZSet(RecommendConstants.KEY_REC_ANONYMOUS, size);
    }

    public List<Long> getColdStartRecommendations(Long userId, int size) {
        String key = RecommendConstants.KEY_REC_COLD + userId;
        return getFromZSet(key, size);
    }

    public void cacheContentSimilarity(Long articleId, Map<Long, Double> similarities) {
        String key = RecommendConstants.KEY_SIM_CONTENT + articleId;
        cacheZSet(key, similarities, RecommendConstants.TTL_SIM_CONTENT);
    }

    public void cacheCFSimilarity(Long articleId, Map<Long, Double> similarities) {
        String key = RecommendConstants.KEY_SIM_CF + articleId;
        cacheZSet(key, similarities, RecommendConstants.TTL_SIM_CF);
    }

    public Map<Long, Double> getCFSimilarity(Long articleId) {
        String key = RecommendConstants.KEY_SIM_CF + articleId;
        return getZSetAsMap(key);
    }

    public void evictUserRecommendations(Long userId) {
        redisTemplate.delete(RecommendConstants.KEY_REC_USER + userId);
        redisTemplate.delete(RecommendConstants.KEY_REC_COLD + userId);
    }

    private void cacheZSet(String key, Map<Long, Double> scores, long ttlSeconds) {
        try {
            redisTemplate.delete(key);
            ZSetOperations<String, Object> zOps = redisTemplate.opsForZSet();
            for (Map.Entry<Long, Double> entry : scores.entrySet()) {
                zOps.add(key, entry.getKey(), entry.getValue());
            }
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存写入失败: key={}", key, e);
        }
    }

    private List<Long> getFromZSet(String key, int size) {
        try {
            Set<Object> results = redisTemplate.opsForZSet().reverseRange(key, 0, size - 1);
            if (results == null || results.isEmpty()) return Collections.emptyList();
            return results.stream()
                    .map(o -> ((Number) o).longValue())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("缓存读取失败: key={}", key, e);
            return Collections.emptyList();
        }
    }

    private Map<Long, Double> getZSetAsMap(String key) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
            if (tuples == null) return Collections.emptyMap();
            Map<Long, Double> result = new HashMap<>();
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                result.put(((Number) tuple.getValue()).longValue(), tuple.getScore());
            }
            return result;
        } catch (Exception e) {
            log.error("缓存读取失败: key={}", key, e);
            return Collections.emptyMap();
        }
    }
}