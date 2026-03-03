package top.hazenix.entity;


import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubAuthorization {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String scope;

}
