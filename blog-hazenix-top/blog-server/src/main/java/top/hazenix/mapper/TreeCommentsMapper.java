package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.Link;
import top.hazenix.entity.TreeComments;
import top.hazenix.query.TreeCommentsQuery;

import java.util.List;

@Mapper
public interface TreeCommentsMapper {


    /**
     * 获取树洞弹幕列表
     * @return
     */
    List<TreeComments> listBulletScreens();


    /**
     * 添加树洞弹幕
     * @param treeComments
     */
    void insert(TreeComments treeComments);

    /**
     * 分页查询树洞弹幕列表
     * @param treeCommentsQuery
     * @return
     */
    Page<TreeComments> pageQuery(TreeCommentsQuery treeCommentsQuery);

    /**
     * 批量删除树洞弹幕
     * @param ids
     */
    void deleteTreeComments(List<Long> ids);
}
