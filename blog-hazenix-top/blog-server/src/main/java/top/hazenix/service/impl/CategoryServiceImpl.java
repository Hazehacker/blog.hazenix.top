package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.dto.CategoryDTO;
import top.hazenix.dto.DeleteCategoryRequestDTO;
import top.hazenix.entity.Article;
import top.hazenix.entity.Category;
import top.hazenix.exception.DeleteNotAllowedException;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.CategoryMapper;
import top.hazenix.result.PageResult;
import top.hazenix.service.CategoryService;

import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 分页查询获取分类列表
     * @param page
     * @param pageSize
     * @param keyword
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer status) {
        PageHelper.startPage(page,pageSize);
        Page<Category> pageRes = categoryMapper.pageQuery(keyword,status);
        return new PageResult(pageRes.getTotal(),pageRes.getResult());
    }

    /**
     * 创建分类
     * @param categoryDTO
     */
    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.insert(category);
    }

    /**
     * 更新分类
     * @param categoryDTO
     */
    @Override
    public void updateCategory(Integer id,CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setId(id);
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     * @param id
     */
    @Override
    public void deleteCategory(Integer id) {
        //如果这个分类关联了文章，就不能删除
        Integer count = articleMapper.countByIds(Collections.singletonList(id));

        if(count != 0){
            throw new DeleteNotAllowedException("当前分类关联了文章，不能删除");
        }
        categoryMapper.deleteBatch(Collections.singletonList(id));
    }

    /**
     * 批量删除分类
     * @param deleteCategoryRequestDTO
     */
    @Override
    public void deleteCategories(DeleteCategoryRequestDTO deleteCategoryRequestDTO) {
        List<Integer> ids = deleteCategoryRequestDTO.getIds();
        //如果这个标签关联了文章，就不能删除
        Integer count = articleMapper.countByIds(ids);
        if(count != 0){
            throw new DeleteNotAllowedException("存在分类关联了文章，不能删除");
        }
        categoryMapper.deleteBatch(ids);
    }


}
