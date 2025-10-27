package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.CommentsDTO;
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
import java.time.LocalDateTime;
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
    @Transactional
    public PageResult pageQuery(Integer page, Integer pageSize, String keyword,Integer status) {
        PageHelper.startPage(page,pageSize);
        Page<Comments> pageRes = commentsMapper.pageQuery(keyword,status);

        List<Comments> list = pageRes.getResult();
        List<CommentsVO> resList = new ArrayList<>();
        for(Comments comments:list){
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comments,commentsVO);
            if (comments.getUserId()!=null) {
                //设置评论者头像
                String avatar = userMapper.getById(comments.getUserId()).getAvatar();
                commentsVO.setAvatar(avatar);
            }
            if (comments.getArticleId()!=null) {
                commentsVO.setArticleTitle(articleMapper.getById(comments.getArticleId()).getTitle());
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

    /**
     * 获取评论列表（用户端根据文章id获取）
     * @param commentsDTO
     */
    @Override
    public List<CommentsVO> getCommentsList(CommentsDTO commentsDTO) {commentsDTO.setStatus(0);
        List<Comments> list = commentsMapper.list(commentsDTO);
        List<CommentsVO> resList = new ArrayList<>();
        for(Comments comments:list){
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comments,commentsVO);
            if (comments.getUserId()!=null) {
                //设置评论者头像
                String avatar = userMapper.getById(comments.getUserId()).getAvatar();
                commentsVO.setAvatar(avatar);
            }
            if (comments.getArticleId()!=null) {
                commentsVO.setArticleTitle(articleMapper.getById(comments.getArticleId()).getTitle());
            }


            resList.add(commentsVO);
        }
        return resList;
    }

    /**
     * 用户新增评论
     * @param commentsDTO
     */
    @Override
    public void addComments(CommentsDTO commentsDTO) {
        Comments comments  = new Comments();
        BeanUtils.copyProperties(commentsDTO,comments);
        comments.setUserId(BaseContext.getCurrentId());
        if(commentsDTO.getReplyId()!=null){
            if (userMapper.getById(commentsDTO.getReplyId())!=null) {
                comments.setReplyUsername(userMapper.getById(commentsDTO.getReplyId()).getUsername());
            }else{
                throw new RuntimeException("不存在该被回复者");
            }
        }
        comments.setStatus(0);
        comments.setCreateTime(LocalDateTime.now());//comments表没有update_time字段，直接这边填充
        commentsMapper.insert(comments);
    }


}
