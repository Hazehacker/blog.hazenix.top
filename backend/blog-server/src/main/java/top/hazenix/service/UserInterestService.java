package top.hazenix.service;

import top.hazenix.entity.UserInterest;
import java.util.List;

public interface UserInterestService {
    void setInterests(Long userId, List<Long> tagIds);
    void updateInterests(Long userId, List<Long> tagIds);
    List<UserInterest> getInterests(Long userId);
    boolean hasInterests(Long userId);
}