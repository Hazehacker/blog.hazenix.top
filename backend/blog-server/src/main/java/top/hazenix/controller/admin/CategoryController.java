package top.hazenix.controller.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.CategoryDTO;

import javax.validation.Valid;
import top.hazenix.dto.DeleteCategoryRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.CategoryService;

import javax.websocket.server.PathParam;

@RestController("AdminCategoryController")
@Slf4j
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {


    private final CategoryService categoryService;

    /**
     * 分页查询，获取分类列表
     * @param page
     * @param pageSize
     * @param keyword
     * @param status
     * @return
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            String keyword,
            Integer status
    ){
        log.info("获取分类列表");
        PageResult pageResult= categoryService.pageQuery(page,pageSize,keyword,status);
        return Result.success(pageResult);
    }

    /**
     * 创建分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "categoriesCache", allEntries = true)
    public Result addCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        log.info("创建分类：{}",categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 更新分类
     * @param id
     * @param categoryDTO
     * @return
     */
    @PutMapping("/{id}")
    @CacheEvict(cacheNames = "categoriesCache", allEntries = true)
    public Result updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryDTO categoryDTO){
        log.info("更新分类：{}",categoryDTO);
        categoryService.updateCategory(id,categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "categoriesCache", allEntries = true)
    public Result deleteCategory(@PathVariable Integer id){
        log.info("删除分类：{}",id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 批量删除分类
     * @param deleteCategoryRequestDTO
     * @return
     */
    @DeleteMapping("/batch")
    @CacheEvict(cacheNames = "categoriesCache", allEntries = true)
    public Result deleteCategories(@Valid @RequestBody DeleteCategoryRequestDTO deleteCategoryRequestDTO){
        log.info("删除分类：{}",deleteCategoryRequestDTO);
        categoryService.deleteCategories(deleteCategoryRequestDTO);

        return Result.success();
    }





}
