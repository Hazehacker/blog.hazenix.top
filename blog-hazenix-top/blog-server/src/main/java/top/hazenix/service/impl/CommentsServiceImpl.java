package top.hazenix.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;

import java.util.Collections;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {


    @Autowired
    private CommentsMapper commentsMapper;

    /**
     * 获取最新评论列表
     * @param i
     * @return
     */
    @Override
    public List<CommentShortVO> getRecentComments(int i) {

        List<CommentShortVO> list = commentsMapper.getRecentComments(i);

        return list;
    }
}
