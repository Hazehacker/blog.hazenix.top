package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.UserInterestMapper;
import top.hazenix.service.UserInterestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInterestServiceImpl implements UserInterestService {

    private final UserInterestMapper userInterestMapper;

    @Override
    @Transactional
    public void setInterests(Long userId, List<Long> tagIds) {
        List<UserInterest> interests = tagIds.stream()
                .map(tagId -> UserInterest.builder()
                        .userId(userId)
                        .tagId(tagId)
                        .weight(RecommendConstants.DEFAULT_INTEREST_WEIGHT)
                        .source(RecommendConstants.INTEREST_SOURCE_REGISTER)
                        .updateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        if (!interests.isEmpty()) {
            userInterestMapper.insertBatch(interests);
        }
    }

    @Override
    @Transactional
    public void updateInterests(Long userId, List<Long> tagIds) {
        userInterestMapper.deleteByUserId(userId);
        setInterests(userId, tagIds);
    }

    @Override
    public List<UserInterest> getInterests(Long userId) {
        return userInterestMapper.getByUserId(userId);
    }

    @Override
    public boolean hasInterests(Long userId) {
        return userInterestMapper.countByUserId(userId) > 0;
    }
}