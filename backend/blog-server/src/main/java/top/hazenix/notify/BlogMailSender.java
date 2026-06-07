package top.hazenix.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.service.NotifyConfigService;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogMailSender {

    private final NotifyConfigService notifyConfigService;

    public void send(String to, String subject, String htmlBody) {
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null) {
            throw new RuntimeException("邮件发送配置未初始化，请先在后台配置 SMTP");
        }
        JavaMailSenderImpl sender = buildSender(config);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(config.getSmtpUsername(), "HazeNix Blog");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            message.addHeader("List-Unsubscribe", "<mailto:" + config.getSmtpUsername() + "?subject=unsubscribe>");
            message.addHeader("X-Mailer", "HazeNix-Blog-Notifier/1.0");
            message.addHeader("Precedence", "bulk");
            sender.send(message);
            log.info("Mail sent to {} subject={}", to, subject);
        } catch (Exception e) {
            throw new RuntimeException("Mail send failed: " + e.getMessage(), e);
        }
    }

    private JavaMailSenderImpl buildSender(NotifyConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort());
        sender.setUsername(config.getSmtpUsername());
        sender.setPassword(notifyConfigService.decryptPassword(config.getSmtpPassword()));
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (config.getSmtpSsl() != null && config.getSmtpSsl() == 1) {
            // 端口 465：直接 SSL/TLS
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            // 端口 587：STARTTLS（Outlook/Gmail 等主流服务商要求）
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        return sender;
    }
}
