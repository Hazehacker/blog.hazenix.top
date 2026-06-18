package top.hazenix.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.MessageConstant;
import top.hazenix.dto.DeleteMomentsDTO;
import top.hazenix.dto.MomentDTO;
import top.hazenix.entity.Moment;
import top.hazenix.entity.MomentLike;
import top.hazenix.entity.MomentTag;
import top.hazenix.exception.BussinessException;
import top.hazenix.mapper.MomentLikeMapper;
import top.hazenix.mapper.MomentMapper;
import top.hazenix.mapper.MomentTagMapper;
import top.hazenix.result.PageResult;
import top.hazenix.service.MomentService;
import top.hazenix.vo.MomentVO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomentServiceImpl implements MomentService {

    private final MomentMapper momentMapper;
    private final MomentTagMapper momentTagMapper;
    private final MomentLikeMapper momentLikeMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void createMoment(MomentDTO dto) {
        Moment moment = Moment.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(serializeImages(dto.getImageUrls()))
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        momentMapper.insert(moment);
        saveTags(moment.getId(), dto.getTags());
    }

    @Override
    @Transactional
    public void updateMoment(Long id, MomentDTO dto) {
        Moment moment = Moment.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(serializeImages(dto.getImageUrls()))
                .status(dto.getStatus())
                .updateTime(LocalDateTime.now())
                .build();
        momentMapper.update(moment);
        momentTagMapper.deleteByMomentId(id);
        saveTags(id, dto.getTags());
    }

    @Override
    @Transactional
    public void deleteMoments(DeleteMomentsDTO dto) {
        momentTagMapper.deleteByMomentIds(dto.getIds());
        momentMapper.deleteBatch(dto.getIds());
    }

    @Override
    public MomentVO getById(Long id, String clientIp) {
        Moment moment = momentMapper.getById(id);
        if (moment == null) {
            throw new BussinessException(ErrorCode.A04001, MessageConstant.NOT_FOUND);
        }
        momentMapper.incrementViewCount(id);
        return buildVO(moment, clientIp);
    }

    @Override
    public PageResult pageQueryUser(Integer page, Integer pageSize, String tagName, String clientIp) {
        PageHelper.startPage(page, pageSize);
        Page<MomentVO> pageRes = momentMapper.pageQuery(null, tagName, 0);
        List<MomentVO> list = pageRes.getResult();
        list.forEach(vo -> fillVO(vo, clientIp));
        return new PageResult(pageRes.getTotal(), list);
    }

    @Override
    public PageResult pageQueryAdmin(Integer page, Integer pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        Page<MomentVO> pageRes = momentMapper.pageQuery(keyword, null, null);
        List<MomentVO> list = pageRes.getResult();
        list.forEach(vo -> fillVO(vo, null));
        return new PageResult(pageRes.getTotal(), list);
    }

    @Override
    public void likeMoment(Long id, String clientIp) {
        MomentLike existing = momentLikeMapper.getByMomentIdAndIp(id, clientIp);
        if (existing != null) {
            throw new BussinessException(ErrorCode.A03003, MessageConstant.ALREADY_LIKED);
        }
        MomentLike like = MomentLike.builder()
                .momentId(id)
                .ip(clientIp)
                .createTime(LocalDateTime.now())
                .build();
        momentLikeMapper.insert(like);
        momentMapper.incrementLikeCount(id);
    }

    @Override
    public List<String> getAllTags() {
        return momentTagMapper.getAllTagNames();
    }

    private void saveTags(Long momentId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;
        List<MomentTag> tags = tagNames.stream()
                .map(name -> MomentTag.builder().momentId(momentId).tagName(name).build())
                .collect(Collectors.toList());
        momentTagMapper.insertBatch(tags);
    }

    private String serializeImages(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> deserializeImages(String images) {
        if (images == null || images.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private MomentVO buildVO(Moment moment, String clientIp) {
        MomentVO vo = new MomentVO();
        vo.setId(moment.getId());
        vo.setTitle(moment.getTitle());
        vo.setContent(moment.getContent());
        vo.setImages(deserializeImages(moment.getImages()));
        vo.setLikeCount(moment.getLikeCount());
        vo.setViewCount(moment.getViewCount());
        vo.setStatus(moment.getStatus());
        vo.setCreateTime(moment.getCreateTime());
        vo.setUpdateTime(moment.getUpdateTime());
        fillVO(vo, clientIp);
        return vo;
    }

    private void fillVO(MomentVO vo, String clientIp) {
        // imagesJson is mapped from `images AS images_json` alias in pageQuery
        if (vo.getImages() == null) {
            vo.setImages(deserializeImages(vo.getImagesJson()));
        }
        vo.setTags(momentTagMapper.getTagNamesByMomentId(vo.getId()));
        if (clientIp != null) {
            vo.setLiked(momentLikeMapper.getByMomentIdAndIp(vo.getId(), clientIp) != null);
        } else {
            vo.setLiked(false);
        }
    }
}
