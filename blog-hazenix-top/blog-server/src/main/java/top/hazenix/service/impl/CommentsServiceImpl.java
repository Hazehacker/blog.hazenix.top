package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.entity.Category;
import top.hazenix.entity.Comments;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.result.PageResult;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;
import top.hazenix.vo.CommentsVO;

import javax.xml.stream.events.Comment;
import java.util.ArrayList;
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

    /**
     * 分页查询获取评论列表
     * @param page
     * @param pageSize
     * @param keyword
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String keyword,Integer status) {
        PageHelper.startPage(page,pageSize);
        Page<Comments> pageRes = commentsMapper.pageQuery(keyword,status);

        List<Comments> list = pageRes.getResult();
        List<CommentsVO> resList = new ArrayList<>();
        for(Comments comments:list){
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comments,commentsVO);
            
        }
        return new PageResult(pageRes.getTotal(),resList);
    }
}
