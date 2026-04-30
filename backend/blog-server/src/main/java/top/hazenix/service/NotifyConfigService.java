package top.hazenix.service;

import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.vo.NotifyConfigVO;

public interface NotifyConfigService {
    NotifyConfigVO getConfig();
    NotifyConfig getRawConfig();
    void updateConfig(NotifyConfigDTO dto);
    String decryptPassword(String encrypted);
}
