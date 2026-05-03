package top.hazenix.service;

public interface ArticleSubscriptionService {
    void subscribe(String email);
    void unsubscribe(String token);
    boolean isSubscribed(String email);
}
