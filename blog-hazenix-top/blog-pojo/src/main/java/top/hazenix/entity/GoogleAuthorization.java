package top.hazenix.entity;


import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Collections;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthorization {
    private GoogleClientSecrets googleClientSecrets;
    private String applicationName;
    private String redirectUrl;
    // 授权域
    private final static List<String> scopes = Collections.singletonList(
            "https://www.googleapis.com/auth/userinfo.email"
    );
    public List<String> getScopes(){
        return scopes;
    }

}
