package top.hazenix.service;

import top.hazenix.dto.DeleteMomentsDTO;
import top.hazenix.dto.MomentDTO;
import top.hazenix.result.PageResult;
import top.hazenix.vo.MomentVO;

import java.util.List;

public interface MomentService {
    void createMoment(MomentDTO momentDTO);
    void updateMoment(Long id, MomentDTO momentDTO);
    void deleteMoments(DeleteMomentsDTO dto);
    MomentVO getById(Long id, String clientIp);
    PageResult pageQueryUser(Integer page, Integer pageSize, String tagName, String clientIp);
    PageResult pageQueryAdmin(Integer page, Integer pageSize, String keyword);
    void likeMoment(Long id, String clientIp);
    List<String> getAllTags();
}
