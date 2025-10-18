package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.dto.ArticleTagsDTO;
import top.hazenix.entity.Article;
import top.hazenix.enumeration.OperationType;
import top.hazenix.vo.ArticleShortVO;

import java.util.List;

@Mapper
public interface ArticleTagsMapper {

    /**
     * 批量插入文章和标签的关联关系
     * @param list
     */
    void insertBatch(List<ArticleTagsDTO> list);
}
