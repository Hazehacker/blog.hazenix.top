package top.hazenix.config;



import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hazenix.entity.GoogleAuthorization;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GoogleAuthorizationConfig {

    @Value("${blog.google.authorization.client.id}")
    private String clientId;

    @Value("${blog.google.authorization.client.secret}")
    private String clientSecret;

    @Value("${blog.google.authorization.application.name}")
    private String applicationName;

    @Value("${blog.google.authorization.redirect.url}")
    private String redirectUrl;

    @Bean(name = "googleAuthorization")
    public GoogleAuthorization googleFeed() {
        GoogleClientSecrets clientSecrets = null;

        try {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(clientId);
            details.setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets();
            clientSecrets.setInstalled(details);
        } catch (Exception e) {
            log.error("authorization configuration error:{}", e.getMessage());
        }

        // 构建bean
        return GoogleAuthorization.builder()
                .googleClientSecrets(clientSecrets)
                .applicationName(applicationName)
                .redirectUrl(redirectUrl)
                .build();
    }
}



