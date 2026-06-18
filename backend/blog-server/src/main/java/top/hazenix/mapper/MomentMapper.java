package top.hazenix.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.Moment;
import top.hazenix.vo.MomentVO;

import java.util.List;

@Mapper
public interface MomentMapper {
    void insert(Moment moment);
    void update(Moment moment);
    void deleteBatch(@Param("ids") List<Long> ids);
    Moment getById(@Param("id") Long id);
    Page<MomentVO> pageQuery(@Param("keyword") String keyword,
                             @Param("tagName") String tagName,
                             @Param("status") Integer status);
    void incrementViewCount(@Param("id") Long id);
    void incrementLikeCount(@Param("id") Long id);
}
