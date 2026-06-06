package top.hazenix.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.entity.Article;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.query.ArticleListQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FeedController {

    private final ArticleMapper articleMapper;

    private static final String SITE_URL = "https://blog.hazenix.top";
    private static final String SITE_TITLE = "HazeNix 的博客";
    private static final String SITE_DESCRIPTION = "记录技术与思考";
    private static final DateTimeFormatter RFC822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    @GetMapping("/feed")
    public void feed(HttpServletResponse response) throws IOException {
        response.setContentType("application/rss+xml; charset=UTF-8");

        ArticleListQuery query = ArticleListQuery.builder().status(0).build();
        List<Article> articles = articleMapper.getArticleList(query);
        if (articles.size() > 20) {
            articles = articles.subList(0, 20);
        }

        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("  <channel>\n");
        xml.append("    <title>").append(escape(SITE_TITLE)).append("</title>\n");
        xml.append("    <link>").append(SITE_URL).append("</link>\n");
        xml.append("    <description>").append(escape(SITE_DESCRIPTION)).append("</description>\n");
        xml.append("    <language>zh-CN</language>\n");
        xml.append("    <atom:link href=\"").append(SITE_URL).append("/feed\"")
           .append(" rel=\"self\" type=\"application/rss+xml\"/>\n");

        for (Article article : articles) {
            String articleId = article.getSlug() != null && !article.getSlug().isBlank()
                    ? article.getSlug()
                    : String.valueOf(article.getId());
            String link = SITE_URL + "/article/" + articleId;

            String description = "";
            if (article.getContent() != null) {
                description = article.getContent().replaceAll("<[^>]+>", "").trim();
                if (description.length() > 300) {
                    description = description.substring(0, 300) + "...";
                }
            }

            String pubDate = "";
            if (article.getCreateTime() != null) {
                ZonedDateTime zdt = article.getCreateTime().atZone(ZoneId.of("Asia/Shanghai"));
                pubDate = RFC822.format(zdt);
            }

            xml.append("    <item>\n");
            xml.append("      <title>").append(escape(article.getTitle())).append("</title>\n");
            xml.append("      <link>").append(link).append("</link>\n");
            xml.append("      <guid isPermaLink=\"true\">").append(link).append("</guid>\n");
            xml.append("      <description><![CDATA[").append(description).append("]]></description>\n");
            if (!pubDate.isEmpty()) {
                xml.append("      <pubDate>").append(pubDate).append("</pubDate>\n");
            }
            xml.append("    </item>\n");
        }

        xml.append("  </channel>\n");
        xml.append("</rss>");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(xml.toString());
        }
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
