package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import top.hazenix.constant.CommonStatusConstants;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.MessageConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.TreeCommentsDTO;
import top.hazenix.entity.TreeComments;
import top.hazenix.exception.BussinessException;
import top.hazenix.mapper.TreeCommentsMapper;
import top.hazenix.mapper.UserMapper;
import top.hazenix.query.TreeCommentsQuery;
import top.hazenix.result.PageResult;
import top.hazenix.service.TreeCommentsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TreeCommentsServiceImpl implements TreeCommentsService {


    private final TreeCommentsMapper treeCommentsMapper;
    private final UserMapper userMapper;

    /**
     * 获取树洞弹幕列表（仅返回已审核通过的）
     * @return
     */
    @Override
    public List<TreeComments> listBulletScreens() {
        return treeCommentsMapper.listBulletScreens();
    }

    /**
     * 添加树洞弹幕
     * 已认证用户：直接发布（status=NORMAL）
     * 匿名用户：进入待审核（status=PENDING）
     * @param treeCommentsDTO
     * @param clientIp
     */
    @Override
    public void addTreeComments(TreeCommentsDTO treeCommentsDTO, String clientIp) {
        TreeComments treeComments = new TreeComments();
        BeanUtils.copyProperties(treeCommentsDTO, treeComments);

        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            treeComments.setUserId(currentUserId);
            treeComments.setUsername(userMapper.getById(currentUserId).getUsername());
            treeComments.setIsAnonymous(false);
            treeComments.setEmail(null);
            treeComments.setIp(null);
            treeComments.setStatus(CommonStatusConstants.NORMAL);
        } else {
            if (treeCommentsDTO.getUsername() == null || treeCommentsDTO.getUsername().trim().isEmpty()) {
                throw new BussinessException(ErrorCode.A03002, MessageConstant.ANONYMOUS_USERNAME_REQUIRED);
            }
            treeComments.setUserId(null);
            treeComments.setUsername(treeCommentsDTO.getUsername().trim());
            treeComments.setEmail(treeCommentsDTO.getEmail());
            treeComments.setIsAnonymous(true);
            treeComments.setIp(clientIp);
            treeComments.setStatus(CommonStatusConstants.PENDING);
        }

        treeComments.setCreateTime(LocalDateTime.now());
        treeCommentsMapper.insert(treeComments);
    }

    @Override
    public PageResult pageQuery(TreeCommentsQuery treeCommentsQuery) {
        PageHelper.startPage(treeCommentsQuery.getPage(), treeCommentsQuery.getPageSize());
        Page<TreeComments> page = treeCommentsMapper.pageQuery(treeCommentsQuery);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteTreeComments(List<Long> ids) {
        treeCommentsMapper.deleteTreeComments(ids);
    }

    @Override
    public void approve(Long id) {
        treeCommentsMapper.updateStatus(id, CommonStatusConstants.NORMAL);
    }

    @Override
    public void reject(Long id) {
        treeCommentsMapper.updateStatus(id, CommonStatusConstants.REJECTED);
    }
}
