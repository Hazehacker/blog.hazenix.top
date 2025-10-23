package top.hazenix.controller.admin;


import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.DeleteCategoryRequestDTO;
import top.hazenix.dto.DeleteTagsRequestDTO;
import top.hazenix.dto.TagsDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.TagsService;

@RestController
@Slf4j
@RequestMapping("/admin/tags")
public class TagsController {

    @Autowired
    private TagsService tagsService;


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
    public Result addTag(@RequestBody TagsDTO tagsDTO){
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
    public Result updateTag(@PathVariable Long id,@RequestBody TagsDTO tagsDTO){
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
    public Result deleteTags(@RequestBody DeleteTagsRequestDTO deleteTagsRequestDTO){
        log.info("批量删除标签");
        try {
            tagsService.deleteTags(deleteTagsRequestDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }




}
