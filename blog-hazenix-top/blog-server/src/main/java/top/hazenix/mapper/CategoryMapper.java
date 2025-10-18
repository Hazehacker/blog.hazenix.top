package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.Category;

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
}
