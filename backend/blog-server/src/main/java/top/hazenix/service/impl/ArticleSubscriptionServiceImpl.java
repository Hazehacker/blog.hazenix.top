package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.service.ArticleSubscriptionService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ArticleSubscriptionServiceImpl implements ArticleSubscriptionService {

    private final ArticleSubscriptionMapper mapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void subscribe(String email) {
        ArticleSubscription existing = mapper.getByEmail(email);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new RuntimeException("该邮箱已订阅");
            }
            // Previously unsubscribed — reactivate with a fresh token
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String newToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            mapper.resubscribe(email, newToken, LocalDateTime.now());
            return;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ArticleSubscription sub = ArticleSubscription.builder()
                .email(email)
                .unsubscribeToken(token)
                .status(1)
                .subscribeAt(LocalDateTime.now())
                .build();
        mapper.insert(sub);
    }

    @Override
    public void unsubscribe(String token) {
        ArticleSubscription sub = mapper.getByUnsubscribeToken(token);
        if (sub == null) {
            throw new RuntimeException("退订链接无效");
        }
        mapper.updateStatus(sub.getEmail(), 2);
    }

    @Override
    public boolean isSubscribed(String email) {
        ArticleSubscription sub = mapper.getByEmail(email);
        return sub != null && sub.getStatus() == 1;
    }
}
