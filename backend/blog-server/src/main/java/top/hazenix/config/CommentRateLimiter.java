package top.hazenix.config;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class CommentRateLimiter {

    private static final int MAX_REQUESTS = 6;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        long cutoff = now - WINDOW_MS;

        Deque<Long> timestamps = buckets.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        Iterator<Long> it = timestamps.iterator();
        while (it.hasNext()) {
            if (it.next() < cutoff) {
                it.remove();
            } else {
                break;
            }
        }

        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
