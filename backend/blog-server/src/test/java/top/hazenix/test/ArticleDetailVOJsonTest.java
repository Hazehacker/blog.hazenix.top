package top.hazenix.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import top.hazenix.vo.ArticleDetailVO;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ArticleDetailVOJsonTest {
    @Test
    public void serializesImagesAsListWithoutConflict() throws Exception {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setImages("[\"a.jpg\"]");              // inherited raw String storage
        vo.setImageList(List.of("a.jpg","b.jpg")); // parsed list
        String json = new ObjectMapper().writeValueAsString(vo); // must NOT throw
        assertTrue(json.contains("\"images\":[\"a.jpg\",\"b.jpg\"]"),
            "images must serialize as the parsed list; got: " + json);
        // the raw JSON-string form must NOT appear as the images value
        assertFalse(json.contains("\"images\":\"["));
    }
}
