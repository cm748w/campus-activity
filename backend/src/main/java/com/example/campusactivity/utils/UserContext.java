package com.example.campusactivity.utils;

import com.example.campusactivity.entity.User;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前登录用户信息，实现线程隔离
 * 在JWT拦截器中设置，在Controller和Service中使用
 */
public class UserContext {

    /**
     * ThreadLocal存储用户对象
     */
    private static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     * @param user 用户对象
     */
    public static void setUser(User user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前登录用户
     * @return 用户对象，未登录返回null
     */
    public static User getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 清除当前线程的用户信息（防止内存泄漏）
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
