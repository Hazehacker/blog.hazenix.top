package top.hazenix.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.CategoryMapper;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.TagsMapper;
import top.hazenix.service.ReportService;
import top.hazenix.vo.StatisticVO;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagsMapper tagsMapper;
    @Autowired
    private CommentsMapper commentsMapper;

    /**
     * 获取仪表盘统计数据，包括总文章数，文章分类数量，文章标签数量，总评论数量
     * @return
     */
    @Override
    public StatisticVO getStatistiic() {
        Integer totalArticles = articleMapper.count();
        Integer totalCategories = categoryMapper.count();
        Integer totalTags = tagsMapper.count();
        Integer totalComments = commentsMapper.count(null);


        return StatisticVO.builder()
                .totalArticles(totalArticles)
                .totalCategories(totalCategories)
                .totalTags(totalTags)
                .totalComments(totalComments)
                .build();
    }






}
