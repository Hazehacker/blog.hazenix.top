package top.hazenix.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 服务 — 为文章标题生成英文 SEO slug。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekService {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${blog.deepseek.api-key}")
    private String apiKey;

    /**
     * 根据文章标题生成英文 URL slug。
     *
     * @param title 文章标题（中文/英文/混合）
     * @return 英文 slug，如 "how-to-write-a-good-resume"
     */
    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }

        String prompt = buildPrompt(title);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "You are a slug generator. Output ONLY the slug, nothing else — no quotes, no explanation, no markdown."),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 50
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, String.class);

            if (response.getBody() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            // 清理输出：去引号、trim、确保全小写
            String slug = content
                    .replaceAll("^[\"'`]+|[\"'`]+$", "")
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9\\-]", "-")
                    .replaceAll("-{2,}", "-")
                    .replaceAll("(^-|-$)", "");

            if (slug.length() > 120) {
                slug = slug.substring(0, 120).replaceAll("-$", "");
            }

            log.info("DeepSeek generated slug: {} -> {}", title, slug);
            return slug.isEmpty() ? null : slug;

        } catch (Exception e) {
            log.error("DeepSeek slug generation failed for title: {}", title, e);
            return null;
        }
    }

    private String buildPrompt(String title) {
        return String.format(
            "Convert the following article title into a short, SEO-friendly English URL slug (3-8 words, lowercase, hyphen-separated, no stop words like 'a', 'the', 'of'). Output ONLY the slug.\n\nTitle: %s", title);
    }
}
