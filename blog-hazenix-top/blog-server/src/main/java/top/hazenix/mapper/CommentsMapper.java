package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
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
}
