package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.MomentLike;

@Mapper
public interface MomentLikeMapper {
    void insert(MomentLike like);
    MomentLike getByArticleIdAndIp(@Param("articleId") Long articleId, @Param("ip") String ip);
}
