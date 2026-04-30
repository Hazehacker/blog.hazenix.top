package top.hazenix.notify;

import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.TreeComments;
import top.hazenix.vo.CommentNotifyVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class NotifyHtmlRenderer {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private NotifyHtmlRenderer() {}

    public static String render(LocalDate statDate,
                                List<CommentNotifyVO> comments,
                                List<TreeComments> treeComments,
                                List<Link> pendingLinks,
                                Map<Long, String[]> linkTokens,
                                String publicBaseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family:sans-serif;background:#f5f5f5;padding:20px'>");
        sb.append("<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.08)'>");

        sb.append("<h2 style='color:#333;border-bottom:2px solid #4A90D9;padding-bottom:8px'>")
          .append("每日通知 · ").append(statDate.format(DATE_FMT)).append("</h2>");

        sb.append("<div style='display:flex;gap:16px;margin:16px 0'>");
        appendBadge(sb, "评论", comments.size(), "#4A90D9");
        appendBadge(sb, "树洞", treeComments.size(), "#7B68EE");
        appendBadge(sb, "友链申请", pendingLinks.size(), "#E67E22");
        sb.append("</div>");

        sb.append("<h3 style='color:#4A90D9'>新增评论（").append(comments.size()).append("）</h3>");
        if (comments.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (CommentNotifyVO c : comments) {
                sb.append("<div style='padding:8px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(c.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(c.getUsername())).append("</b>：");
                sb.append(escapeHtml(truncate(c.getContent())));
                if (c.getArticleTitle() != null) {
                    sb.append(" <span style='color:#888'>｜ 文章《").append(escapeHtml(c.getArticleTitle())).append("》</span>");
                }
                sb.append("</div>");
            }
        }

        sb.append("<h3 style='color:#7B68EE'>新增树洞评论（").append(treeComments.size()).append("）</h3>");
        if (treeComments.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (TreeComments t : treeComments) {
                sb.append("<div style='padding:8px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(t.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(t.getUsername())).append("</b>：");
                sb.append(escapeHtml(truncate(t.getContent())));
                sb.append("</div>");
            }
        }

        sb.append("<h3 style='color:#E67E22'>待审核友链申请（").append(pendingLinks.size()).append("）</h3>");
        if (pendingLinks.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (Link l : pendingLinks) {
                sb.append("<div style='padding:12px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(l.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(l.getName())).append("</b>");
                sb.append(" / <a href='").append(escapeHtml(l.getUrl())).append("'>").append(escapeHtml(l.getUrl())).append("</a>");
                if (l.getDescription() != null) {
                    sb.append("<br><span style='color:#666'>").append(escapeHtml(truncate(l.getDescription()))).append("</span>");
                }
                String[] tokens = linkTokens.get(l.getId());
                if (tokens != null) {
                    String approveUrl = publicBaseUrl + "/api/notify/link-action?token=" + tokens[0];
                    String rejectUrl = publicBaseUrl + "/api/notify/link-action?token=" + tokens[1];
                    sb.append("<div style='margin-top:8px'>");
                    sb.append("<a href='").append(approveUrl).append("' style='display:inline-block;padding:6px 16px;background:#27ae60;color:#fff;text-decoration:none;border-radius:4px;margin-right:8px'>✅ 通过</a>");
                    sb.append("<a href='").append(rejectUrl).append("' style='display:inline-block;padding:6px 16px;background:#e74c3c;color:#fff;text-decoration:none;border-radius:4px'>❌ 拒绝</a>");
                    sb.append("</div>");
                }
                sb.append("</div>");
            }
        }

        sb.append("<p style='color:#bbb;font-size:12px;margin-top:24px;text-align:center'>此邮件由系统自动发送，请勿回复</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private static void appendBadge(StringBuilder sb, String label, int count, String color) {
        sb.append("<div style='background:").append(color).append("1a;padding:8px 16px;border-radius:6px;text-align:center'>");
        sb.append("<div style='font-size:24px;font-weight:bold;color:").append(color).append("'>").append(count).append("</div>");
        sb.append("<div style='font-size:12px;color:#666'>").append(label).append("</div></div>");
    }

    private static String truncate(String text) {
        if (text == null) return "";
        return text.length() > NotifyConstants.CONTENT_SNIPPET_LENGTH
                ? text.substring(0, NotifyConstants.CONTENT_SNIPPET_LENGTH) + "…"
                : text;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
