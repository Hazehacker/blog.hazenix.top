package top.hazenix.mapper;


import org.apache.ibatis.annotations.Mapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.entity.User;
import top.hazenix.enumeration.OperationType;

@Mapper
public interface UserMapper {

    User getById(Long id);

    /**
     * 根据邮箱获取用户账号信息
     * @param email
     * @return
     */
    User selectByEmail(String email);

    /**
     * 插入用户信息
     * @param user
     */
    @AutoFill(OperationType.INSERT)
    void insert(User user);

    /**
     * 更新用户信息
     * @param user
     */
    @AutoFill(OperationType.UPDATE)
    void update(User user);

    /**
     * 仅更新最近登录时间
     * @param userUse
     */
    void updateLastLoginTime(User userUse);
}
