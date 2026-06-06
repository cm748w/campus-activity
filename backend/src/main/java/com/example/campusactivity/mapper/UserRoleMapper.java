package com.example.campusactivity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusactivity.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联数据访问层
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
