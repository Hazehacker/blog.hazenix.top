package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagsMapper {

    /**
     * 统计文章标签数量
     * @return
     */
    Integer count();
}
