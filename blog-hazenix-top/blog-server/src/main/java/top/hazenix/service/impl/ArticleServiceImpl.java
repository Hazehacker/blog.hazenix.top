package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.ArticleDTO;
import top.hazenix.dto.ArticleTagsDTO;
import top.hazenix.entity.Article;
import top.hazenix.entity.Category;
import top.hazenix.entity.Tags;
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
     * 获取文章列表(主要用于用户端)
     * @param
     * @return
     */
    @Override
    public List<ArticleDetailVO> getArticleList(ArticleListQuery articleListQuery) {

        List<Article> list = articleMapper.getArticleList(articleListQuery);
        List<ArticleDetailVO> listRes = new ArrayList<>();
        for(Article article:list){
            ArticleDetailVO articleDetailVO = new ArticleDetailVO();
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
            listRes.add(articleDetailVO);
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
