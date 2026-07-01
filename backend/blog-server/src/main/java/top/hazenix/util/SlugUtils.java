package top.hazenix.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 文章 slug 自动生成工具。
 * <p>
 * 将中文标题转为拼音 slug，英文保留原样，特殊字符替换为连字符。
 * 示例："简历焚诀：如何编写优化简历" → "jian-li-fen-jue-ru-he-bian-xie-you-hua-jian-li"
 */
public final class SlugUtils {

    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = new HanyuPinyinOutputFormat();

    static {
        PINYIN_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        PINYIN_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        PINYIN_FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private SlugUtils() {}

    /**
     * 从标题生成 URL 友好的 slug。
     *
     * @param title 文章标题（中文/英文/混合）
     * @return slug，如 "jian-li-fen-jue"
     */
    public static String generate(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            char ch = title.charAt(i);
            // 中文字符 → 拼音
            if (Character.toString(ch).matches("[\\u4e00-\\u9fa5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, PINYIN_FORMAT);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        sb.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination ignored) {
                }
            }
            // 英文字母 → 小写
            else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                sb.append(Character.toLowerCase(ch));
            }
            // 数字 → 保留
            else if (ch >= '0' && ch <= '9') {
                sb.append(ch);
            }
            // 空格、标点、特殊字符 → 连字符
            else {
                // 跳过前导/重复连字符的逻辑在下面统一处理
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') {
                    sb.append('-');
                }
            }
        }

        // 清理：去掉首尾连字符，合并连续连字符
        String slug = sb.toString().replaceAll("-{2,}", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            return null;
        }
        // 限制长度，避免 URL 过长
        if (slug.length() > 120) {
            slug = slug.substring(0, 120).replaceAll("-$", "");
        }
        return slug;
    }

    /**
     * 附加数字后缀确保唯一。
     * <p>
     * 调用方需要提供 slug 唯一性检查逻辑。
     * 如果 slug 已存在，追加 "-2"、"-3"... 直到不冲突。
     *
     * @param baseSlug 基础 slug
     * @param existsFn 检查 slug 是否已存在的函数
     * @return 唯一的 slug
     */
    public static String ensureUnique(String baseSlug, java.util.function.Predicate<String> existsFn) {
        if (baseSlug == null || !existsFn.test(baseSlug)) {
            return baseSlug;
        }
        int suffix = 2;
        String candidate;
        do {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        } while (existsFn.test(candidate) && suffix < 1000);
        return candidate;
    }
}
