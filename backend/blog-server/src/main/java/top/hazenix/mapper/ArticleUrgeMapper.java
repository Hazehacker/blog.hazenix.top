package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.ArticleUrge;
import java.util.List;

@Mapper
public interface ArticleUrgeMapper {
    ArticleUrge getByMonth(@Param("month") String month);
    void insert(ArticleUrge urge);
    void incrementCount(@Param("month") String month);
    List<ArticleUrge> listAll();
}