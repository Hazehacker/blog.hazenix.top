package top.hazenix.service;


import top.hazenix.dto.CommentsDTO;
import top.hazenix.result.PageResult;
import top.hazenix.vo.CommentShortVO;
import top.hazenix.vo.CommentsVO;

import java.util.List;

public interface CommentsService {
    List<CommentShortVO> getRecentComments(int i);

    PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer status);


    void deleteComments(List<Long> ids);

    List<CommentsVO> getCommentsList(CommentsDTO commentsDTO);

    void addComments(CommentsDTO commentsDTO);
}
