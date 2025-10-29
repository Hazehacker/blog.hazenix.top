package top.hazenix.service;

import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.entity.Link;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.result.PageResult;

import java.util.List;

public interface LinkService {
    PageResult listLinks(LinkQueryDTO linkQueryDTO);

    Link getLinkById(Long id);

    void addLink(LinkDTO linkDTO);

    void updateLink(Long id, LinkDTO linkDTO);

    void deleteLink(Long id);

    void deleteLinks(DeleteLinksRequestDTO deleteLinksRequestDTO);

    void updateLinkStatus(Long id);

    List<Link> listLinksUserSide();
}
