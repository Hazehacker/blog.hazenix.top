package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanMetadataAttribute;
import top.hazenix.entity.Tags;

import java.util.List;

@Mapper
public interface TagsMapper {

    /**
     * 统计文章标签数量
     * @return
     */
    Integer count();

    /**
     * 根据文章id获取标签id列表
     * @param id
     * @return
     */
    List<Integer> getListByArticleId(Long id);

    /**
     * 根据id查询标签（返回tags对象）
     * @param tagId
     * @return
     */
    Tags getById(Integer tagId);
}
