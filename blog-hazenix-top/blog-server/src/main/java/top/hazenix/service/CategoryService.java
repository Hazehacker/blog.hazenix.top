package top.hazenix.service;

import top.hazenix.dto.CategoryDTO;
import top.hazenix.dto.DeleteCategoryRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer status);

    void addCategory(CategoryDTO categoryDTO);

    void updateCategory(Integer id,CategoryDTO categoryDTO);

    void deleteCategory(Integer id);

    void deleteCategories(DeleteCategoryRequestDTO deleteCategoryRequestDTO);

    List<CategoryVO> getCategoryList();
}
