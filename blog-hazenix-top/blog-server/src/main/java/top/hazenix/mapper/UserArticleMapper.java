package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.User;
import top.hazenix.entity.UserArticle;

@Mapper
public interface UserArticleMapper {

    /**
     * 用于判断当前用户和当前文章是否执行过点赞/收藏逻辑(是否已经有过历史数据)
     * @param userId
     * @param
     * @return
     */
    UserArticle getByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 更新数据
     * @param userArticle
     */
    void update(UserArticle userArticle);
    /**
     * 关联表插入数据
     * @param userArticleInsertUse
     */
    void insert(UserArticle userArticleInsertUse);

    /**
     * 获取当前用户收藏数
     * @param userId
     * @return
     */
    Integer getFavoriteCount(Long userId);

    /**
     * 获取当前用户点赞数
     * @param userId
     * @return
     */
    Integer getLikeCount(Long userId);
}
