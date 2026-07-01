package top.hazenix.test;

import org.junit.jupiter.api.Test;
import top.hazenix.util.MomentTitleUtil;
import static org.junit.jupiter.api.Assertions.*;

public class MomentTitleUtilTest {
    @Test
    public void keepsExplicitTitle() {
        assertEquals("你好", MomentTitleUtil.fallbackTitle("你好", "正文很长很长"));
    }
    @Test
    public void fallsBackToContentExcerptWhenTitleBlank() {
        String r = MomentTitleUtil.fallbackTitle("  ", "今天写了并入文章的方案，感觉不错");
        assertEquals("今天写了并入文章的方案，感觉不错", r);
    }
    @Test
    public void truncatesLongContentTo30Chars() {
        String content = "一二三四五六七八九十".repeat(4); // 40 字
        String r = MomentTitleUtil.fallbackTitle(null, content);
        assertEquals(30, r.length() - 1); // 30 + 省略号
        assertTrue(r.endsWith("…"));
    }
    @Test
    public void handlesNullContent() {
        assertEquals("无标题", MomentTitleUtil.fallbackTitle(null, null));
    }
}
