package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.User;

@Mapper
public interface UserMapper {

    User getById(Long id);
}
