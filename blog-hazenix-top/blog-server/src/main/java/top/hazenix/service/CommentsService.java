package top.hazenix.service;


import top.hazenix.result.PageResult;
import top.hazenix.vo.CommentShortVO;

import java.util.List;

public interface CommentsService {
    List<CommentShortVO> getRecentComments(int i);

    PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer status);
}
