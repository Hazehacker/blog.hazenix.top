package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.ArticleDTO;
import top.hazenix.dto.ArticleTagsDTO;
import top.hazenix.entity.Article;
import top.hazenix.entity.Category;
import top.hazenix.entity.Tags;
import top.hazenix.entity.UserArticle;
import top.hazenix.mapper.*;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.PageResult;
import top.hazenix.service.ArticleService;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagsMapper tagsMapper;
    @Autowired
    private ArticleTagsMapper articleTagsMapper;
    @Autowired
    private CommentsMapper commentsMapper;
    @Autowired
    private UserArticleMapper userArticleMapper;

    /**
     * 获取最新点的文章列表
     * @param i
     * @return
     */
    @Override
    public List<ArticleShortVO> getRecentArticles(int i) {
        List<ArticleShortVO> list = articleMapper.getRecentArticles(i);
        return list;
    }

    /**
     * 分页查询文章列表（可以根据关键词、分类、文章状态过滤）
     * @param page
     * @param pageSize
     * @param keyword
     * @param categoryId
     * @param status
     * @return
     */
    @Override
    @Transactional
    public PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer categoryId, Integer status) {
        PageHelper.startPage(page,pageSize);
        Page<ArticleShortVO> pageRes = articleMapper.pageQuery(keyword,categoryId,status);
        List<ArticleShortVO> list = pageRes.getResult();

        if (categoryId != null) {
            for(ArticleShortVO vo:list){
                Category temp = categoryMapper.getById(categoryId);
                Category category = Category.builder()
                        .id(categoryId)
                        .name(temp.getName())
                        .build();
                vo.setCategory(category);
            }
        }
        return new PageResult(pageRes.getTotal(),list);
    }

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ArticleDetailVO getArticleDetail(Long id) {
        Article article = articleMapper.getById(id);
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article,articleDetailVO);
        //查询这篇文章对应的标签
        List<Integer> tagsId = tagsMapper.getListByArticleId(id);
        List<Tags> tags = tagsId.stream()
                .map(tagId -> {
                    String tagName = tagsMapper.getById(tagId).getName();
                    return Tags.builder()
                            .id(Long.valueOf(tagId))
                            .name(tagName)
                            .build();
                })
                .collect(Collectors.toList());
        articleDetailVO.setTags(tags);
        articleDetailVO.setCategoryName(categoryMapper.getById(article.getCategoryId()).getName());
        articleDetailVO.setCommentCount(commentsMapper.count(id));
        Long userId = BaseContext.getCurrentId();
        if(userId != null){
            UserArticle userArticle = userArticleMapper.getByUserIdAndArticleId(userId, id);
            if(userArticle != null){
                articleDetailVO.setIsLiked(userArticle.getIsLiked());
                articleDetailVO.setIsFavorite(userArticle.getIsFavorite());
            }
        }
        return articleDetailVO;
    }
    //还有什么能比自己手搓博客更爽的呢？tell me looking in my eyes
    //成就感真的太强了，写个功能都爽死



    /**
     * 新增文章
     * @param articleDTO
     */
    @Override
    @Transactional
    public void addArticle(ArticleDTO articleDTO) {
        //TODO (检验slug是否已经在数据库中存在)
        //插入article表
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO,article);
        article.setUserId(BaseContext.getCurrentId());
        if (article.getUserId() == null) {
            article.setUserId(1L);
        }
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setViewCount(1);
        article.setIsTop(0);//默认不指定
        article.setStatus(articleDTO.getStatus());
        articleMapper.insert(article);

        Long id = article.getId();
        //插入article_tags表
        if (articleDTO.getTagIds()!=null && articleDTO.getTagIds().size()>0) {
            List<Integer> tagIds = articleDTO.getTagIds();
            List<ArticleTagsDTO> list = new ArrayList<>();
            for(Integer tagId : tagIds){
                //查询这个tagId对应的名字
                String tagName = tagsMapper.getById(tagId).getName();
                ArticleTagsDTO articleTagsDTO = ArticleTagsDTO.builder()
                        .articleId(id)
                        .tagsId(tagId)
                        .tagsName(tagName)
                        .build();
                list.add(articleTagsDTO);
            }
            articleTagsMapper.insertBatch(list);
        }


    }

    /**
     * 更新文章
     * @param id
     * @param articleDTO
     */
    @Override
    public void updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = new Article();
        article.setId(id);
        BeanUtils.copyProperties(articleDTO,article);
        articleMapper.update(article);
    }



    /**
     * 更新文章状态
     * @param id
     * @param status
     */
    @Override
    public void updateArticleStatus(Long id, Integer status) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        articleMapper.update(article);
    }
    /**
     * (用户浏览之后触发)更新文章浏览量
     * @param id
     */
    @Override
    public void updateArticleView(Long id) {
        Article article = articleMapper.getById(id);
        Article articleUse = Article.builder()
                .id(id)
                .viewCount(article.getViewCount()+1)
                .build();
        articleMapper.update(articleUse);
    }

    /**
     * 用户点赞文章
     * @param id
     */
    @Override
    public void likeArticle(Long id) {
        //获得当前用户的id
        Long userId = BaseContext.getCurrentId();

        //如果用户没登录，就直接增加点赞数后返回
        if(userId == null){//TODO 不确定没登录的用户判断逻辑是不是这个（是否userId是null）（登录功能做了之后再看看）
            Article article = Article.builder()
                    .id(id)
                    .likeCount(articleMapper.getById(id).getLikeCount()+1)
                    .build();
            articleMapper.update(article);
            return;
        }else{
            //查询关联表中是否已经有了这个用户和这篇文章的数据
            UserArticle userArticle = userArticleMapper.getByUserIdAndArticleId(userId,id);
            if(userArticle != null){
                Article article = Article.builder()
                        .id(id)
                        .build();

                //已经有了，执行更新逻辑
                Integer isLiked = userArticle.getIsLiked();
                if(isLiked == 1){
                    //已经点过赞了，执行取消点赞逻辑
                    userArticle.setIsLiked(0);
                    userArticleMapper.update(userArticle);
                    //并减少文章的点赞数
//                    Article article = Article.builder()
//                            .id(id)
//                            .likeCount(articleMapper.getById(id).getLikeCount()-1)
//                            .build();
//                    articleMapper.update(article);
                    article.setLikeCount(articleMapper.getById(id).getLikeCount()-1);
                }else{
                    //没有点过赞，执行点赞逻辑；并增加文章的点赞数
                    userArticle.setIsLiked(1);
                    userArticleMapper.update(userArticle);
//                    Article article = Article.builder()
//                            .id(id)
//                            .likeCount(articleMapper.getById(id).getLikeCount()+1)
//                            .build();
//                    articleMapper.update(article);
                    article.setLikeCount(articleMapper.getById(id).getLikeCount()+1);
                }

                articleMapper.update(article);



            }else{
                //还没有条目，执行插入；文章点赞数增加
                UserArticle userArticleInsertUse = UserArticle.builder()
                        .userId(userId)
                        .articleId(id)
                        .isLiked(1)
                        .isFavorite(0)
                        .build();
                userArticleMapper.insert(userArticleInsertUse);
                Article article = Article.builder()
                            .id(id)
                            .likeCount(articleMapper.getById(id).getLikeCount()+1)
                            .build();
                articleMapper.update(article);
            }
        }

    }

    /**
     * 用户收藏文章
     * @param id
     */
    @Override
    public void favoriteArticle(Long id) {
        //获得当前用户的id
        Long userId = BaseContext.getCurrentId();

        //如果用户没登录，不能收藏
        if(userId == null){//TODO 不确定没登录的用户判断逻辑是不是这个（是否userId是null）（登录功能做了之后再看看）
            throw new RuntimeException("登录后才能收藏文章");

        }else{
            //查询关联表中是否已经有了这个用户和这篇文章的数据
            UserArticle userArticle = userArticleMapper.getByUserIdAndArticleId(userId,id);
            if(userArticle != null){
                Article article = Article.builder()
                        .id(id)
                        .build();

                //已经有了，执行更新逻辑
                Integer isFavorite = userArticle.getIsLiked();
                if(isFavorite == 1){
                    //已经收藏了，执行取消收藏逻辑
                    userArticle.setIsLiked(0);
                    userArticleMapper.update(userArticle);
                    //并减少文章的点赞数
                    article.setFavoriteCount(articleMapper.getById(id).getFavoriteCount()-1);
                }else{
                    //没有收藏，执行收藏逻辑；并增加文章的收藏数
                    userArticle.setIsLiked(1);
                    userArticleMapper.update(userArticle);

                    article.setFavoriteCount(articleMapper.getById(id).getFavoriteCount()+1);
                }

                articleMapper.update(article);



            }else{
                //还没有条目，执行插入；文章收藏数增加
                UserArticle userArticleInsertUse = UserArticle.builder()
                        .userId(userId)
                        .articleId(id)
                        .isLiked(0)
                        .isFavorite(1)
                        .build();
                userArticleMapper.insert(userArticleInsertUse);
                Article article = Article.builder()
                        .id(id)
                        .likeCount(articleMapper.getById(id).getFavoriteCount()+1)
                        .build();
                articleMapper.update(article);
            }
        }
    }

    /**
     * 获取文章列表(主要用于用户端)
     * @param
     * @return
     */
    @Override
    public List<ArticleDetailVO> getArticleList(ArticleListQuery articleListQuery) {


        //后面查询user_article表要用到
        Long userId = BaseContext.getCurrentId();


        List<Article> list = articleMapper.getArticleList(articleListQuery);
        if(list == null){
            throw new RuntimeException("不存在相关文章");
        }
        List<ArticleDetailVO> listRes = new ArrayList<>();
        for(Article article:list){
            ArticleDetailVO articleDetailVO = new ArticleDetailVO();
            try {
                BeanUtils.copyProperties(article,articleDetailVO);
                articleDetailVO.setCategoryName(categoryMapper.getById(article.getCategoryId()).getName());
                List<Integer> tagIds = tagsMapper.getListByArticleId(article.getId());
                List<Tags> tags = tagIds.stream()
                        .map(tagId -> {
                            String tagName = tagsMapper.getById(tagId).getName();
                            return Tags.builder()
                                    .id(Long.valueOf(tagId))
                                    .name(tagName)
                                    .build();
                        })
                        .collect(Collectors.toList());
                articleDetailVO.setTags(tags);
                articleDetailVO.setCategoryName(categoryMapper.getById(article.getCategoryId()).getName());
                articleDetailVO.setCommentCount(commentsMapper.count(article.getId()));


                //查询user_article表(查询文章列表、文章详细信息的时候带上线程里面的id，然后查user_article表，如果当前用户的iS_liked字段为1，
                // 则返回值中的isLiked设为1)
                if(userId != null){
                    UserArticle userArticle = userArticleMapper.getByUserIdAndArticleId(userId, article.getId());
                    if(userArticle != null){
                        articleDetailVO.setIsLiked(userArticle.getIsLiked());
                        articleDetailVO.setIsFavorite(userArticle.getIsFavorite());
                    }
                }

                listRes.add(articleDetailVO);
            } catch (BeansException e) {
                e.printStackTrace();
            }
        }
        return listRes;
    }

    /**
     * 根据slug获取文章详情
     * @param slug
     * @return
     */
    @Override
    public ArticleDetailVO getArticleDetailBySlug(String slug) {
        Article article = articleMapper.getBySlug(slug);//【】
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article,articleDetailVO);
        //查询这篇文章对应的标签
        List<Integer> tagsId = tagsMapper.getListByArticleId(article.getId());
        List<Tags> tags = tagsId.stream()
                .map(tagId -> {
                    String tagName = tagsMapper.getById(tagId).getName();
                    return Tags.builder()
                            .id(Long.valueOf(tagId))
                            .name(tagName)
                            .build();
                })
                .collect(Collectors.toList());
        articleDetailVO.setTags(tags);
        articleDetailVO.setCategoryName(categoryMapper.getById(article.getCategoryId()).getName());
        articleDetailVO.setCommentCount(commentsMapper.count(article.getId()));

        Long userId = BaseContext.getCurrentId();
        if(userId != null){
            UserArticle userArticle = userArticleMapper.getByUserIdAndArticleId(userId, article.getId());
            if(userArticle != null){
                articleDetailVO.setIsLiked(userArticle.getIsLiked());
                articleDetailVO.setIsFavorite(userArticle.getIsFavorite());
            }
        }
        return articleDetailVO;



    }

    /**
     * 获取热门文章
     * @param i
     * @return
     */
    @Override
    public List<ArticleShortVO> getPopularArticles(int i) {
        List<Article> articleList = articleMapper.getPopularArticles(i);
        List<ArticleShortVO> list = articleList.stream()
                .map(article -> {
                    ArticleShortVO articleShortVO = new ArticleShortVO();
                    BeanUtils.copyProperties(article,articleShortVO);
                    return articleShortVO;
                })
                .collect(Collectors.toList());
        //不用添加category，热门排行榜展示用不到

        return list;
    }



    /**
     * 删除单个文章
     * @param id
     */
    @Override
    public void deleteArticle(Long id) {
        //删除文章的同时，也要维护article_tags关联表
        articleTagsMapper.deleteByArticleIds(Collections.singletonList(id));
        //和这篇文章有关系的评论也要删掉
        commentsMapper.deleteByArticleIds(Collections.singletonList(id));
        articleMapper.deleteByIds(Collections.singletonList(id));
    }

    /**
     * 批量删除文章
     * @param ids
     */
    @Override
    @Transactional
    public void deleteArticles(List<Long> ids) {
        //删除文章的同时，也要维护article_tags关联表
        articleTagsMapper.deleteByArticleIds(ids);
        // TODO 和这篇文章有关系的评论也要删掉
        commentsMapper.deleteByArticleIds(ids);
        articleMapper.deleteByIds(ids);
    }



}
