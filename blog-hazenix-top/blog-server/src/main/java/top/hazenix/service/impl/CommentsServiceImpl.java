package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.entity.Article;
import top.hazenix.entity.Category;
import top.hazenix.entity.Comments;
import top.hazenix.entity.User;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.UserMapper;
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
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;

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
            //设置评论者头像
            String avatar = userMapper.getById(comments.getUserId()).getAvatar();
            commentsVO.setAvatar(avatar);
            Article article = Article.builder()
                            .id(comments.getArticleId())
                    .title(articleMapper.getById(comments.getArticleId()).getTitle())
                                    .build();
            commentsVO.setArticle(article);
            User replyPerson = new User();
            if (comments.getReplyId() != null) {
                replyPerson.setId(comments.getReplyId());
                replyPerson.setUsername(comments.getReplyUsername());
                commentsVO.setReplyPerson(replyPerson);
            }
            resList.add(commentsVO);
        }

        return new PageResult(pageRes.getTotal(),resList);
    }

    /**
     * 批量删除评论
     * @param ids
     */
    @Override
    public void deleteComments(List<Long> ids) {
        commentsMapper.deleteBatch(ids);
    }


}
