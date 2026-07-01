package top.hazenix.util;

public final class MomentTitleUtil {
    private MomentTitleUtil() {}

    /** 手记标题兜底：优先显式标题，其次正文首 30 字摘要，末尾加省略号 */
    public static String fallbackTitle(String title, String content) {
        if (title != null && !title.isBlank()) return title;
        if (content == null || content.isBlank()) return "无标题";
        String plain = content.strip();
        if (plain.length() <= 30) return plain;
        return plain.substring(0, 30) + "…";
    }
}
