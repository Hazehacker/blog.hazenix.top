package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.entity.Link;
import top.hazenix.entity.User;
import top.hazenix.enumeration.OperationType;

import java.util.List;

@Mapper
public interface LinkMapper {

    /**
     * 分页查询
     * @param link
     * @return
     */
    Page<Link> pageQuery(Link link);

    Link getLinkById(Long id);

    /**
     * 新增友链
     * @param link
     */
    void insert(Link link);

    /**
     * 修改友链
     * @param link
     */
    void updateLink(Link link);

    /**
     * 批量删除友链
     * @param ids
     */
    void deleteLinks(List<Long> ids);

    List<Link> list();
}
