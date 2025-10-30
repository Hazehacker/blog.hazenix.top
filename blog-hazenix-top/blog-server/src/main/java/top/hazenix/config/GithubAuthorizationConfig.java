package top.hazenix.config;




import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hazenix.entity.GithubAuthorization;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubAuthorizationConfig {

    @Value("${blog.github.authorization.client.id}")
    private String clientId;

    @Value("${blog.github.authorization.client.secret}")
    private String clientSecret;

//    @Value("${blog.google.authorization.application.name}")
//    private String applicationName;

    @Value("${blog.github.authorization.redirect.url}")
    private String redirectUrl;

    @Bean(name = "githubAuthorization")
    public GithubAuthorization githubFeed() {


        // 构建bean
        return GithubAuthorization.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUrl(redirectUrl)
                .build();
    }
}



