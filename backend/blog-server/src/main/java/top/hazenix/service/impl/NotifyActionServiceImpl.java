package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.NotifyActionToken;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.mapper.NotifyActionTokenMapper;
import top.hazenix.service.NotifyActionService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyActionServiceImpl implements NotifyActionService {

    private final NotifyActionTokenMapper tokenMapper;
    private final LinkMapper linkMapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String issueToken(Long targetId, String targetType, String action) {
        byte[] bytes = new byte[NotifyConstants.TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        NotifyActionToken entity = NotifyActionToken.builder()
                .token(token)
                .targetType(targetType)
                .targetId(targetId)
                .action(action)
                .expiresAt(LocalDateTime.now().plusDays(NotifyConstants.TOKEN_EXPIRE_DAYS))
                .createTime(LocalDateTime.now())
                .build();
        tokenMapper.insert(entity);
        return token;
    }

    @Override
    @Transactional
    public String consumeToken(String token) {
        NotifyActionToken entity = tokenMapper.getByToken(token);
        if (entity == null || entity.getUsedAt() != null || entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return renderHtml("链接无效或已失效", "该链接已使用、已过期或不存在。");
        }

        tokenMapper.markUsedByTargetId(entity.getTargetType(), entity.getTargetId());

        if ("link".equals(entity.getTargetType())) {
            Link link = linkMapper.getLinkById(entity.getTargetId());
            if (link == null) {
                return renderHtml("操作失败", "友链不存在。");
            }
            String linkName = link.getName();
            if ("approve".equals(entity.getAction())) {
                Link update = Link.builder().id(entity.getTargetId()).status(0).build();
                linkMapper.updateLink(update);
                log.info("Link approved via token: id={} name={}", entity.getTargetId(), linkName);
                return renderHtml("审核通过", "已通过友链申请：" + linkName);
            } else if ("reject".equals(entity.getAction())) {
                Link update = Link.builder().id(entity.getTargetId()).status(2).build();
                linkMapper.updateLink(update);
                log.info("Link rejected via token: id={} name={}", entity.getTargetId(), linkName);
                return renderHtml("已拒绝", "已拒绝友链申请：" + linkName);
            }
        }
        return renderHtml("操作失败", "不支持的操作类型。");
    }

    private String renderHtml(String title, String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title
                + "</title><style>body{font-family:sans-serif;display:flex;justify-content:center;"
                + "align-items:center;height:100vh;margin:0;background:#f5f5f5}"
                + ".card{background:#fff;padding:40px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.1);"
                + "text-align:center;max-width:400px}</style></head><body><div class='card'><h2>"
                + title + "</h2><p>" + message + "</p></div></body></html>";
    }
}
