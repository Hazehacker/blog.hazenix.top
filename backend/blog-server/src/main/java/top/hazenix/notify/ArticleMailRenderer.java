package top.hazenix.notify;

public class ArticleMailRenderer {
    private static final int SUMMARY_LENGTH = 120;

    private ArticleMailRenderer() {}

    public static String render(String title, String summary, Long articleId, String unsubscribeToken) {
        String articleUrl = "https://blog.hazenix.top/article/" + articleId;
        String unsubscribeUrl = "https://blog.hazenix.top/api/unsubscribe?token=" + unsubscribeToken;
        String truncated = (summary != null && summary.length() > SUMMARY_LENGTH)
                ? summary.substring(0, SUMMARY_LENGTH) + "…"
                : (summary != null ? summary : "");
        return "<!DOCTYPE html><html><body style='font-family:sans-serif;max-width:600px;margin:0 auto;padding:20px'>"
                + "<h2 style='color:#333'>《" + escapeHtml(title) + "》</h2>"
                + "<p style='color:#555;font-size:16px'>" + escapeHtml(truncated) + "</p>"
                + "<p><a href='" + articleUrl + "' style='display:inline-block;padding:10px 24px;background:#4A90D9;color:#fff;text-decoration:none;border-radius:4px'>阅读全文 →</a></p>"
                + "<hr style='margin:24px 0;border:none;border-top:1px solid #eee'>"
                + "<p style='font-size:12px;color:#999'>"
                + "<a href='https://blog.hazenix.top/feed' style='color:#4A90D9'>RSS 订阅</a> | "
                + "<a href='" + unsubscribeUrl + "' style='color:#999'>退订通知</a>"
                + "</p></body></html>";
    }

    public static String renderSubscribeConfirm(String email, String unsubscribeToken) {
        String unsubscribeUrl = "https://blog.hazenix.top/api/unsubscribe?token=" + unsubscribeToken;
        return "<!DOCTYPE html><html><body style='font-family:sans-serif;max-width:600px;margin:0 auto;padding:20px'>"
                + "<h2 style='color:#333'>订阅成功</h2>"
                + "<p style='color:#555;font-size:15px'>你好！</p>"
                + "<p style='color:#555;font-size:15px'>你的邮箱 <strong>" + escapeHtml(email) + "</strong> 已成功订阅 Hazenix Blog 文章更新通知。</p>"
                + "<p style='color:#555;font-size:15px'>新文章发布后，你将收到邮件推送，第一时间阅读。</p>"
                + "<hr style='margin:24px 0;border:none;border-top:1px solid #eee'>"
                + "<p style='font-size:12px;color:#999'>"
                + "<a href='https://blog.hazenix.top/feed' style='color:#4A90D9'>RSS 订阅</a> | "
                + "<a href='" + unsubscribeUrl + "' style='color:#999'>退订通知</a>"
                + "</p></body></html>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
