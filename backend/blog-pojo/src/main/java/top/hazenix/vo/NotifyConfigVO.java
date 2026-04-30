package top.hazenix.vo;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class NotifyConfigVO {
    private String recipient;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Integer smtpSsl;
    private String sendTime;
    private Integer enabled;
}
