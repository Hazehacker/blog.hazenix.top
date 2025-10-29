package top.hazenix.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.entity.Link;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.result.PageResult;
import top.hazenix.service.LinkService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
public class LinkServiceImpl implements LinkService {
    @Autowired
    private LinkMapper linkMapper;



    /**
     * 获取友链列表
     * @param linkQueryDTO
     * @return
     */
    @Override
    public PageResult listLinks(LinkQueryDTO linkQueryDTO) {
        PageHelper.startPage(linkQueryDTO.getPage(), linkQueryDTO.getPageSize());
        Link link = Link.builder()
                .name(linkQueryDTO.getKeyword())
                .status(linkQueryDTO.getStatus())
                .build();
        Page<Link> page = linkMapper.pageQuery(link);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 获取友链详情
     * @param id
     * @return
     */
    @Override
    public Link getLinkById(Long id) {
        return linkMapper.getLinkById(id);
    }

    /**
     * 新增友链
     * @param linkDTO
     */
    @Override
    public void addLink(LinkDTO linkDTO) {
        Link link = new Link();
        BeanUtils.copyProperties(linkDTO,link);
        link.setCreateTime(LocalDateTime.now());
        linkMapper.insert(link);
    }

    /**
     * 更新友链
     * @param id
     * @param linkDTO
     */
    @Override
    public void updateLink(Long id, LinkDTO linkDTO) {
        Link link = new Link();
        BeanUtils.copyProperties(linkDTO,link);
        link.setId(id);
        linkMapper.updateLink(link);
    }

    /**
     * 删除友链
     * @param id
     */
    @Override
    public void deleteLink(Long id) {
        linkMapper.deleteLinks(Collections.singletonList(id));
    }

    @Override
    public void deleteLinks(DeleteLinksRequestDTO deleteLinksRequestDTO) {
        List<Long> ids = deleteLinksRequestDTO.getIds();
        linkMapper.deleteLinks(ids);

    }

    /**
     * 更新友链状态
     * @param id
     */
    @Override
    public void updateLinkStatus(Long id) {
        Integer status = (linkMapper.getLinkById(id).getStatus()==1) ? 0:1;
        Link link = Link.builder()
                .id( id)
                .status(status)
                .build();
        linkMapper.updateLink(link);
    }

    @Override
    public List<Link> listLinksUserSide() {
        List<Link> list = linkMapper.list();
        return list;
    }
}
