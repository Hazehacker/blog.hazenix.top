package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.mapper.NotifyConfigMapper;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.utils.AesCryptoUtil;
import top.hazenix.vo.NotifyConfigVO;

@Service
@RequiredArgsConstructor
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private final NotifyConfigMapper notifyConfigMapper;

    @Value("${blog.notify.encrypt-key}")
    private String encryptKey;

    @Override
    public NotifyConfigVO getConfig() {
        NotifyConfig config = notifyConfigMapper.getById(NotifyConstants.CONFIG_ID);
        if (config == null) return null;
        return NotifyConfigVO.builder()
                .recipient(config.getRecipient())
                .smtpHost(config.getSmtpHost())
                .smtpPort(config.getSmtpPort())
                .smtpUsername(config.getSmtpUsername())
                .smtpPassword(NotifyConstants.PASSWORD_MASK)
                .smtpSsl(config.getSmtpSsl())
                .sendTime(config.getSendTime())
                .enabled(config.getEnabled())
                .build();
    }

    @Override
    public NotifyConfig getRawConfig() {
        return notifyConfigMapper.getById(NotifyConstants.CONFIG_ID);
    }

    @Override
    public void updateConfig(NotifyConfigDTO dto) {
        NotifyConfig config = NotifyConfig.builder()
                .id(NotifyConstants.CONFIG_ID)
                .recipient(dto.getRecipient())
                .smtpHost(dto.getSmtpHost())
                .smtpPort(dto.getSmtpPort())
                .smtpUsername(dto.getSmtpUsername())
                .smtpSsl(dto.getSmtpSsl())
                .sendTime(dto.getSendTime())
                .enabled(dto.getEnabled())
                .build();
        if (dto.getSmtpPassword() != null && !dto.getSmtpPassword().isEmpty()) {
            config.setSmtpPassword(AesCryptoUtil.encrypt(dto.getSmtpPassword(), encryptKey));
        }
        notifyConfigMapper.update(config);
    }

    @Override
    public String decryptPassword(String encrypted) {
        return AesCryptoUtil.decrypt(encrypted, encryptKey);
    }
}
