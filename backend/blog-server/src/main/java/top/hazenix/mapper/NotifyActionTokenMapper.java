package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.NotifyActionToken;

@Mapper
public interface NotifyActionTokenMapper {
    void insert(NotifyActionToken token);
    NotifyActionToken getByToken(@Param("token") String token);
    void markUsed(@Param("token") String token);
    void markUsedByTargetId(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
