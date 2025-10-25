package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.dto.DeleteTagsRequestDTO;
import top.hazenix.dto.TagsDTO;
import top.hazenix.entity.Category;
import top.hazenix.entity.Tags;
import top.hazenix.exception.DeleteNotAllowedException;
import top.hazenix.mapper.ArticleTagsMapper;
import top.hazenix.mapper.TagsMapper;
import top.hazenix.result.PageResult;
import top.hazenix.service.TagsService;
import top.hazenix.vo.TagsVO;

import java.util.Collections;
import java.util.List;

@Service
public class TagsServiceImpl implements TagsService {
    @Autowired
    private TagsMapper tagsMapper;
    @Autowired
    private ArticleTagsMapper articleTagsMapper;

    /**
     * 分页查询标签列表
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String keyword ,Integer status) {
        PageHelper.startPage(page,pageSize);
        Page<Category> pageRes = tagsMapper.pageQuery(keyword,status);
        return new PageResult(pageRes.getTotal(),pageRes.getResult());
    }

    /**
     * 新增标签
     * @param tagsDTO
     */
    @Override
    public void addTag(TagsDTO tagsDTO) {
        Tags tags = new Tags();
        BeanUtils.copyProperties(tagsDTO,tags);
        if(tags.getStatus()==null){
            tags.setStatus(1);//默认禁用
        }
        tagsMapper.insert(tags);
    }

    /**
     * 更新指定标签
     * @param id
     * @param tagsDTO
     */
    @Override
    public void updateTag(Long id, TagsDTO tagsDTO) {
        Tags tags = new Tags();
        BeanUtils.copyProperties(tagsDTO,tags);
        tags.setId(id);
        tagsMapper.update(tags);
        //更新的时候也要更新article_tags表
        articleTagsMapper.updateTagsName(id,tags.getName());
    }

    /**
     * 删除标签
     * @param id
     */
    @Override
    public void deleteTag(Long id) {
        //如果这个标签关联了文章，就不能删除
        Integer count = articleTagsMapper.countByIds(Collections.singletonList(id));
        if(count != 0){
            throw new DeleteNotAllowedException("当前标签关联了文章，不能删除");
        }
        tagsMapper.deleteBatch(Collections.singletonList(id));
    }

    /**
     * 获取标签列表（用户端）
     * @return
     */
    @Override
    public List<TagsVO> getTagsList() {
       List<TagsVO> list = tagsMapper.list(null,0);
       return list;
    }


    /**
     * 批量删除标签
     * @param deleteTagsRequestDTO
     */
    @Override
    public void deleteTags(DeleteTagsRequestDTO deleteTagsRequestDTO) {
        List<Long> ids = deleteTagsRequestDTO.getIds();
        //如果这个标签关联了文章，就不能删除
        Integer count = articleTagsMapper.countByIds(ids);
        if(count != 0){
            throw new DeleteNotAllowedException("存在标签关联了文章，不能删除");
        }

        tagsMapper.deleteBatch(ids);
    }


}
