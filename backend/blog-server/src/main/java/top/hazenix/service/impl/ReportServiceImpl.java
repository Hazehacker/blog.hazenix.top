package top.hazenix.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.CategoryMapper;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.TagsMapper;
import top.hazenix.service.ReportService;
import top.hazenix.vo.StatisticVO;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {


    private final ArticleMapper articleMapper;

    private final CategoryMapper categoryMapper;

    private final TagsMapper tagsMapper;

    private final CommentsMapper commentsMapper;

    /**
     * 获取仪表盘统计数据，包括总文章数，文章分类数量，文章标签数量，总评论数量
     * @return
     */
    @Override
    public StatisticVO getStatistiic() {
        Integer totalArticles = articleMapper.count();
        Integer totalCategories = categoryMapper.count();
        Integer totalTags = tagsMapper.count(null);
        Integer totalComments = commentsMapper.count(null);


        return StatisticVO.builder()
                .totalArticles(totalArticles)
                .totalCategories(totalCategories)
                .totalTags(totalTags)
                .totalComments(totalComments)
                .build();
    }






}
