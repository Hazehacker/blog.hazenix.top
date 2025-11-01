package top.hazenix.service;

import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.dto.TreeCommentsDTO;
import top.hazenix.entity.Link;
import top.hazenix.entity.TreeComments;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.query.TreeCommentsQuery;
import top.hazenix.result.PageResult;

import java.util.List;

public interface TreeCommentsService {

    List<TreeComments> listBulletScreens();

    void addTreeComments(TreeCommentsDTO treeCommentsDTO);

    PageResult pageQuery(TreeCommentsQuery treeCommentsQuery);

    void deleteTreeComments(List<Long> ids);
}
