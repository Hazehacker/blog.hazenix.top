package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.NotifyConfig;

@Mapper
public interface NotifyConfigMapper {
    NotifyConfig getById(Long id);
    void update(NotifyConfig config);
}
