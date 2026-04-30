package top.hazenix.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyLog;
import top.hazenix.mapper.NotifyLogMapper;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.task.NotifyScheduleManager;
import top.hazenix.vo.NotifyConfigVO;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/notify")
@RequiredArgsConstructor
public class NotifyConfigController {

    private final NotifyConfigService notifyConfigService;
    private final DailyNotifyService dailyNotifyService;
    private final NotifyScheduleManager notifyScheduleManager;
    private final NotifyLogMapper notifyLogMapper;

    @GetMapping("/config")
    public Result<NotifyConfigVO> getConfig() {
        return Result.success(notifyConfigService.getConfig());
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(@Valid @RequestBody NotifyConfigDTO dto) {
        notifyConfigService.updateConfig(dto);
        notifyScheduleManager.reschedule();
        return Result.success();
    }

    @PostMapping("/test")
    public Result<Void> testSend() {
        dailyNotifyService.sendTestMail();
        return Result.success();
    }

    @PostMapping("/trigger")
    public Result<Void> trigger(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        dailyNotifyService.runForDate(date);
        return Result.success();
    }

    @GetMapping("/log")
    public Result<PageResult> getLogs(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size) {
        PageHelper.startPage(page, size);
        Page<NotifyLog> logs = notifyLogMapper.pageQuery();
        return Result.success(new PageResult(logs.getTotal(), logs.getResult()));
    }
}
