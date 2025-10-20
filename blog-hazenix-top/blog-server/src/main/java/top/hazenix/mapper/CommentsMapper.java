package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.Category;
import top.hazenix.entity.Comments;
import top.hazenix.vo.CommentShortVO;

import java.util.List;

@Mapper
public interface CommentsMapper {

    /**
     * 统计评论总数
     * @return
     */
    Integer count();

    /**
     * 获取最新评论列表
     * @param i
     * @return
     */
    List<CommentShortVO> getRecentComments(int i);

    /**
     * 分页查询，获取评论列表
     * @param keyword
     * @param status
     * @return
     */
    Page<Comments> pageQuery(String keyword, Integer status);

    /**
     * 批量删除评论
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据文章ids集合删除评论
     * @param ids
     */
    void deleteByArticleIds(List<Long> ids);
}
