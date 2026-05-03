package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.SiteLike;
import java.util.List;

@Mapper
public interface SiteLikeMapper {
    void insert(SiteLike like);
    SiteLike getByIpHash(@Param("ipHash") String ipHash);
    Long countAll();
    Long countToday();
    List<SiteLike> listRecent(@Param("limit") Integer limit);
}