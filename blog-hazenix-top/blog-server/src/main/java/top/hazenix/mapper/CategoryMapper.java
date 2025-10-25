package top.hazenix.mapper;


import com.github.pagehelper.Page;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.entity.Category;
import top.hazenix.enumeration.OperationType;
import top.hazenix.vo.CategoryVO;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 统计 文章分类 数量
     * @return
     */
    Integer count();

    /**
     * 根据id获取分类(返回category对象)
     * @param categoryId
     * @return
     */
    Category getById(Integer categoryId);

    /**
     * (分页查询)获取分类列表
     * @param keyword
     * @param status
     * @return
     */
    Page<Category> pageQuery(String keyword, Integer status);


    /**
     * 获取分类列表
     * @return
     */
    List<CategoryVO> getCategoryList(String name);

    /**
     * 创建分类
     * @param category
     */
    @AutoFill(OperationType.INSERT)
    void insert(Category category);

    /**
     * 修改分类信息
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void update(Category category);



    /**
     * 批量删除分类
     * @param ids
     */
    void deleteBatch(List<Integer> ids);



}
