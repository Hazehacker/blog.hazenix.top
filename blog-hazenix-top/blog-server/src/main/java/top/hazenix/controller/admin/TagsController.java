package top.hazenix.controller.admin;


import com.github.pagehelper.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.*;
import top.hazenix.dto.DeleteCategoryRequestDTO;

import javax.validation.Valid;
import top.hazenix.dto.DeleteTagsRequestDTO;
import top.hazenix.dto.TagsDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.TagsService;

@RestController("AdminTagsController")
@Slf4j
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class TagsController {


    private final TagsService tagsService;


    /**
     * 分页查询标签列表
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping
    public Result<PageResult> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String keyword,
            Integer status
    ) {
        log.info("分页查询标签列表");
        PageResult pageResult = tagsService.pageQuery(page,pageSize,keyword,status);
        return Result.success(pageResult);
    }

    /**
     * 新增标签
     * @param tagsDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "tagsCache",allEntries = true)
    public Result addTag(@Valid @RequestBody TagsDTO tagsDTO){
        log.info("新增标签");
        tagsService.addTag(tagsDTO);
        return Result.success();
    }

    /**
     * 更新指定标签
     * @param tagsDTO
     * @return
     */
    @PutMapping("/{id}")
    @CacheEvict(cacheNames = "tagsCache",allEntries = true)
    public Result updateTag(@PathVariable Long id, @Valid @RequestBody TagsDTO tagsDTO){
        log.info("更新制定标签");
        tagsService.updateTag(id,tagsDTO);
        return Result.success();
    }

    /**
     * 删除标签
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "tagsCache",allEntries = true)
    public Result deleteTag(@PathVariable Long id){
        log.info("删除标签");
        tagsService.deleteTag(id);
        return Result.success();
    }

    /**
     * 批量删除标签
     * @param deleteTagsRequestDTO
     * @return
     */
    @DeleteMapping("/batch")
    @CacheEvict(cacheNames = "tagsCache",allEntries = true)
    public Result deleteTags(@Valid @RequestBody DeleteTagsRequestDTO deleteTagsRequestDTO){
        log.info("批量删除标签");
        tagsService.deleteTags(deleteTagsRequestDTO);

        return Result.success();
    }




}
