package top.hazenix.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.DeleteMomentsDTO;
import top.hazenix.dto.MomentDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.MomentService;

import javax.validation.Valid;

@RestController("AdminMomentController")
@RequestMapping("/admin/moment")
@Slf4j
@RequiredArgsConstructor
public class MomentAdminController {

    private final MomentService momentService;

    @GetMapping("/page")
    public Result<PageResult> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("后台分页查询手记, keyword={}", keyword);
        PageResult result = momentService.pageQueryAdmin(page, pageSize, keyword);
        return Result.success(result);
    }

    @PostMapping
    public Result create(@Valid @RequestBody MomentDTO momentDTO) {
        log.info("新建手记");
        momentService.createMoment(momentDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @Valid @RequestBody MomentDTO momentDTO) {
        log.info("更新手记, id={}", id);
        momentService.updateMoment(id, momentDTO);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result delete(@Valid @RequestBody DeleteMomentsDTO dto) {
        log.info("批量删除手记, ids={}", dto.getIds());
        momentService.deleteMoments(dto);
        return Result.success();
    }
}
