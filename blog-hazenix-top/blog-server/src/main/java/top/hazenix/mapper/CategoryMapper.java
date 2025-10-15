package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {

    /**
     * 统计 文章分类 数量
     * @return
     */
    Integer count();
}
