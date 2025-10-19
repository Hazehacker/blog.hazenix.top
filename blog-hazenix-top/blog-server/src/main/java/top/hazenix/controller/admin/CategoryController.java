package top.hazenix.controller.admin;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.CategoryDTO;
import top.hazenix.dto.DeleteCategoryRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.CategoryService;

import javax.websocket.server.PathParam;

@RestController
@Slf4j
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

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
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
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
    public Result updateCategory(@PathVariable Integer id,@RequestBody CategoryDTO categoryDTO){
        log.info("更新分类：{}",categoryDTO);
        categoryService.updateCategory(id,categoryDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Integer id){
        log.info("删除分类：{}",id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result deleteCategories(@RequestBody DeleteCategoryRequestDTO deleteCategoryRequestDTO){
        log.info("删除分类：{}",deleteCategoryRequestDTO);
        categoryService.deleteCategories(deleteCategoryRequestDTO);

        return Result.success();
    }





}
