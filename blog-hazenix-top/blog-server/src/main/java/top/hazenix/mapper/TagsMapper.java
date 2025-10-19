package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanMetadataAttribute;
import top.hazenix.annotation.AutoFill;
import top.hazenix.dto.TagsDTO;
import top.hazenix.entity.Category;
import top.hazenix.entity.Tags;
import top.hazenix.enumeration.OperationType;

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

    /**
     * 分页查询标签列表
     * @param keyword
     * @return
     */
    Page<Category> pageQuery(String keyword);

    /**
     * 新增标签
     * @param tags
     */
    @AutoFill(OperationType.INSERT)
    void insert(Tags tags);

    /**
     * 更新指定标签
     * @param tags
     */
    @AutoFill(OperationType.UPDATE)
    void update(Tags tags);

    /**
     * 批量删除标签
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
