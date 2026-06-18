package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.MomentTag;

import java.util.List;

@Mapper
public interface MomentTagMapper {
    void insertBatch(@Param("tags") List<MomentTag> tags);
    void deleteByMomentId(@Param("momentId") Long momentId);
    void deleteByMomentIds(@Param("ids") List<Long> ids);
    List<String> getTagNamesByMomentId(@Param("momentId") Long momentId);
    List<String> getAllTagNames();
}
