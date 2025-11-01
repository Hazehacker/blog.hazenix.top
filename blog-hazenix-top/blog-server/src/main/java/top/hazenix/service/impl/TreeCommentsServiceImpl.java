package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.dto.TreeCommentsDTO;
import top.hazenix.entity.Link;
import top.hazenix.entity.TreeComments;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.mapper.TreeCommentsMapper;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.query.TreeCommentsQuery;
import top.hazenix.result.PageResult;
import top.hazenix.service.LinkService;
import top.hazenix.service.TreeCommentsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
public class TreeCommentsServiceImpl implements TreeCommentsService {

    @Autowired
    private TreeCommentsMapper treeCommentsMapper;
    /**
     * 获取树洞弹幕列表
     * @return
     */
    @Override
    public List<TreeComments> listBulletScreens() {

        List<TreeComments> list = treeCommentsMapper.listBulletScreens();

        return list;
    }

    /**
     * 添加树洞弹幕
     * @param treeCommentsDTO
     */
    @Override
    public void addTreeComments(TreeCommentsDTO treeCommentsDTO) {
        TreeComments treeComments = new TreeComments();
        BeanUtils.copyProperties(treeCommentsDTO,treeComments);
//        treeComments.setUserId(treeCommentsDTO.getUserId());
        treeComments.setStatus(0);
        treeComments.setCreateTime(LocalDateTime.now());
        treeCommentsMapper.insert(treeComments);
    }

    @Override
    public PageResult pageQuery(TreeCommentsQuery treeCommentsQuery) {
        PageHelper.startPage(treeCommentsQuery.getPage(),treeCommentsQuery.getPageSize());
        Page<TreeComments> page = treeCommentsMapper.pageQuery(treeCommentsQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除树洞弹幕
     * @param ids
     */
    @Override
    public void deleteTreeComments(List<Long> ids) {
        treeCommentsMapper.deleteTreeComments(ids);
    }
}
