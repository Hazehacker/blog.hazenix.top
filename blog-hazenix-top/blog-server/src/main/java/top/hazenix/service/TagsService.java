package top.hazenix.service;

import top.hazenix.dto.DeleteTagsRequestDTO;
import top.hazenix.dto.TagsDTO;
import top.hazenix.result.PageResult;
import top.hazenix.vo.TagsVO;

import java.util.List;

public interface TagsService {
    PageResult pageQuery(Integer page, Integer pageSize, String keyword ,Integer status);

    void addTag(TagsDTO tagsDTO);

    void updateTag(Long id, TagsDTO tagsDTO);

    void deleteTags(DeleteTagsRequestDTO deleteTagsRequestDTO);

    void deleteTag(Long id);

    List<TagsVO> getTagsList();
}
