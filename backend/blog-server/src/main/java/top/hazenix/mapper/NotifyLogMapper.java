package top.hazenix.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.NotifyLog;

@Mapper
public interface NotifyLogMapper {
    void insert(NotifyLog log);
    Page<NotifyLog> pageQuery();
}
