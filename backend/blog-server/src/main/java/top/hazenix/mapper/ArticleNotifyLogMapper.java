package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.ArticleNotifyLog;

@Mapper
public interface ArticleNotifyLogMapper {
    void insert(ArticleNotifyLog log);
}
