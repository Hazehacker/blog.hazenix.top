package top.hazenix.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NotifyConfigDTO {
    @NotBlank @Email
    private String recipient;
    @NotBlank
    private String smtpHost;
    @NotNull
    private Integer smtpPort;
    @NotBlank
    private String smtpUsername;
    private String smtpPassword;
    private Integer smtpSsl;
    @NotBlank
    private String sendTime;
    @NotNull
    private Integer enabled;
}
