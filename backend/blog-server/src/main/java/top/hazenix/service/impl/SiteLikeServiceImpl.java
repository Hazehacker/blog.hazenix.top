package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.SiteLike;
import top.hazenix.mapper.SiteLikeMapper;
import top.hazenix.service.SiteLikeService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SiteLikeServiceImpl implements SiteLikeService {

    private final SiteLikeMapper mapper;

    @Override
    public long likeAndGetCount(String ipHash) {
        SiteLike existing = mapper.getByIpHash(ipHash);
        if (existing != null) {
            throw new RuntimeException("已喜欢过");
        }
        mapper.insert(SiteLike.builder().ipHash(ipHash).createdAt(LocalDateTime.now()).build());
        return mapper.countAll();
    }

    @Override
    public long getTotalCount() {
        return mapper.countAll();
    }

    @Override
    public long getTodayCount() {
        return mapper.countToday();
    }
}
