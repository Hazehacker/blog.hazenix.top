package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.ArticleSubscription;
import java.util.List;

@Mapper
public interface ArticleSubscriptionMapper {
    void insert(ArticleSubscription sub);
    ArticleSubscription getByEmail(@Param("email") String email);
    ArticleSubscription getByUnsubscribeToken(@Param("token") String token);
    List<ArticleSubscription> listActive();
    void updateStatus(@Param("email") String email, @Param("status") Integer status);
    com.github.pagehelper.Page<ArticleSubscription> pageQuery();
    ArticleSubscription getById(@Param("id") Long id);
}