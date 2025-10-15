package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import springfox.documentation.oas.mappers.LicenseMapper;
import top.hazenix.vo.ArticleShortVO;

import java.util.List;

@Mapper
public interface ArticleMapper {


    /**
     * 统计文章数量
     * @return
     */
    Integer count();

    List<ArticleShortVO> getRecentArticles(int i);
}
