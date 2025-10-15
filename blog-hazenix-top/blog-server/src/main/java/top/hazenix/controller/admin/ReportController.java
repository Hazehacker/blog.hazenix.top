package top.hazenix.controller.admin;


import com.sun.org.glassfish.external.statistics.Statistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.result.Result;
import top.hazenix.service.ReportService;
import top.hazenix.vo.StatisticVO;

@RestController
@Slf4j
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private ReportService reportService;
    /**
     * 获取仪表盘统计数据，包括总文章数，文章分类数量，文章标签数量，总评论数量
     * @return
     */
    @GetMapping("/stats")
    public Result<StatisticVO> getStatistic() {
        StatisticVO statisticVO = reportService.getStatistiic();

        return Result.success(statisticVO);
    }

}
