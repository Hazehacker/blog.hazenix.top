package top.hazenix.service;

public interface NotifyActionService {
    String issueToken(Long targetId, String targetType, String action);
    String consumeToken(String token);
}
