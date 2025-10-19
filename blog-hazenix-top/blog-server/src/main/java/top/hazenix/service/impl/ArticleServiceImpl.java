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
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.ArticleTagsMapper;
import top.hazenix.mapper.CategoryMapper;
import top.hazenix.mapper.TagsMapper;
import top.hazenix.result.PageResult;
import top.hazenix.service.ArticleService;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        articleDetailVO.setTagsId(tagsId);

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
        //插入article表
        Article article = new Article();
        article.setUserId(BaseContext.getCurrentId());
        if (article.getUserId() == null) {
            article.setUserId(1L);
        }
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setViewCount(1);
        article.setIsTop(0);//默认不指定
        BeanUtils.copyProperties(articleDTO,article);
        article.setStatus(articleDTO.getStatus());
        articleMapper.insert(article);

        Long id = article.getId();
        //插入article_tags表
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
     * 删除单个文章
     * @param id
     */
    @Override
    public void deleteArticle(Long id) {
        //删除文章的同时，也要维护article_tags关联表
        articleTagsMapper.deleteByArticleIds(Collections.singletonList(id));

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

        articleMapper.deleteByIds(ids);
    }



}
