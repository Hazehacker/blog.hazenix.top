package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.UserInterest;
import java.util.List;

@Mapper
public interface UserInterestMapper {
    void insert(UserInterest interest);

    void insertBatch(@Param("list") List<UserInterest> list);

    void update(UserInterest interest);

    void deleteByUserId(@Param("userId") Long userId);

    List<UserInterest> getByUserId(@Param("userId") Long userId);

    UserInterest getByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    int countByUserId(@Param("userId") Long userId);
}