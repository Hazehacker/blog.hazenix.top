package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.ArticleUrge;
import top.hazenix.mapper.ArticleUrgeMapper;
import top.hazenix.service.ArticleUrgeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleUrgeServiceImpl implements ArticleUrgeService {

    private final ArticleUrgeMapper mapper;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public int urgeAndGetCount() {
        String month = LocalDate.now().format(MONTH_FMT);
        ArticleUrge record = mapper.getByMonth(month);
        if (record == null) {
            mapper.insert(ArticleUrge.builder().urgeMonth(month).count(1).build());
            return 1;
        }
        mapper.incrementCount(month);
        return record.getCount() + 1;
    }

    @Override
    public int getCurrentMonthCount() {
        String month = LocalDate.now().format(MONTH_FMT);
        ArticleUrge record = mapper.getByMonth(month);
        return record == null ? 0 : record.getCount();
    }

    @Override
    public List<ArticleUrge> getStats() {
        return mapper.listAll();
    }
}
