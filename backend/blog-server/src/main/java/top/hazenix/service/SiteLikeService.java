package top.hazenix.service;

public interface SiteLikeService {
    long likeAndGetCount(String ipHash);
    long getTotalCount();
    long getTodayCount();
}
