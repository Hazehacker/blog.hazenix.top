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
     * 统计标签id集合下的文章数量
     * @param ids
     * @return
     */
    Integer countByIds(List<Long> ids);

    /**
     * 批量插入文章和标签的关联关系
     * @param list
     */
    void insertBatch(List<ArticleTagsDTO> list);

    /**
     * 在更新标签的同时更新关联表中的tags_name字段
     * @param id
     * @param tagsName
     */
    void updateTagsName(Long id, String tagsName);

    /**
     * 删除文章的同时删除关联表中对应的条目
     * @param ids
     */
    void deleteByArticleIds(List<Long> ids);
}
