package top.hazenix.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/subscription")
@RequiredArgsConstructor
public class SubscriptionAdminController {

    private final ArticleSubscriptionMapper mapper;

    @GetMapping("/list")
    public Result<PageResult> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size) {
        PageHelper.startPage(page, size);
        Page<ArticleSubscription> p = mapper.pageQuery();
        return Result.success(new PageResult(p.getTotal(), p.getResult()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ArticleSubscription sub = mapper.getById(id);
        if (sub != null) {
            mapper.updateStatus(sub.getEmail(), 2);  // 2=已退订
        }
        return Result.success();
    }

    @GetMapping("/export")
    public Result<java.util.List<String>> export() {
        java.util.List<ArticleSubscription> subs = mapper.listActive();
        java.util.List<String> emails = subs.stream().map(ArticleSubscription::getEmail).collect(Collectors.toList());
        return Result.success(emails);
    }
}