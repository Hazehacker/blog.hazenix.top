package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.UserBehavior;
import java.util.List;

@Mapper
public interface UserBehaviorMapper {
    void insert(UserBehavior behavior);

    List<UserBehavior> getByUserId(@Param("userId") Long userId);

    List<UserBehavior> getByArticleId(@Param("articleId") Long articleId);

    int countByUserId(@Param("userId") Long userId);

    /** 获取所有有行为的用户ID */
    List<Long> getAllActiveUserIds();

    /** 获取所有有行为的文章ID */
    List<Long> getAllActiveArticleIds();

    /** 获取指定文章的行为数量 */
    int countByArticleId(@Param("articleId") Long articleId);

    /** 获取用户对文章的最高评分行为 */
    List<UserBehavior> getByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 获取所有行为数据（用于协同过滤全量计算） */
    List<UserBehavior> listAll();
}