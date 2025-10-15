package top.hazenix.service;

import top.hazenix.vo.CommentShortVO;

import java.util.List;

public interface CommentsService {
    List<CommentShortVO> getRecentComments(int i);
}
