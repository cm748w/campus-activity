package com.example.campusactivity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusactivity.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据访问层
 * 提供用户相关的数据库操作方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（包含角色信息）
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT u.*, r.role_code, r.role_name FROM user u " +
            "LEFT JOIN user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE u.username = #{username} AND u.deleted = 0")
    User selectByUsernameWithRole(String username);

    /**
     * 分页查询用户列表（带角色信息）
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param roleCode 角色编码筛选
     * @return 分页结果
     */
    IPage<User> selectUserPage(Page<User> page, 
                               @Param("keyword") String keyword, 
                               @Param("roleCode") String roleCode);

    /**
     * 禁用/启用用户账号
     * @param userId 用户ID
     * @param status 目标状态
     */
    @Update("UPDATE user SET status = #{status} WHERE id = #{userId}")
    void updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
