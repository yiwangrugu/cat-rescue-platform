package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.entity.Favorite;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.repository.CatRepository;
import com.catrescue.catrescueplatform.repository.FavoriteRepository;
import com.catrescue.catrescueplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CatRepository catRepository;
    private final UserRepository userRepository;

    /**
     * 收藏猫咪
     */
    public void favoriteCat(Long catId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("猫咪收藏操作 - Authentication: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("猫咪收藏操作失败: 用户未登录");
            throw new RuntimeException("用户未登录");
        }

        String username;
        Object principal = authentication.getPrincipal();
        System.out.println("猫咪收藏操作 - Principal type: " + principal.getClass().getName());
        System.out.println("猫咪收藏操作 - Principal: " + principal);

        if (principal instanceof String) {
            username = (String) principal;
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else {
            System.out.println("猫咪收藏操作失败: Principal不是String或User类型");
            System.out.println("Principal类型: " + principal.getClass().getName());
            throw new RuntimeException("无法获取用户信息");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            System.out.println("猫咪收藏操作失败: 用户不存在 - " + username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();
        System.out.println("猫咪收藏操作 - 用户ID: " + userId + ", 猫咪ID: " + catId);

        // 检查是否已经收藏
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("cat_id", catId)
                .eq("type", "CAT")
                .last("LIMIT 1");

        Favorite existingFavorite = favoriteRepository.selectOne(wrapper);
        if (existingFavorite != null) {
            System.out.println("猫咪收藏操作失败: 已经收藏过该猫咪");
            throw new RuntimeException("已经收藏过该猫咪");
        }

        // 创建新的收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCatId(catId);
        favorite.setType("CAT");
        favorite.setCreateTime(LocalDateTime.now());

        favoriteRepository.insert(favorite);
        System.out.println("猫咪收藏操作成功: 插入收藏记录");

        // 更新用户的收藏数
        if (user != null) {
            user.setFavoriteCount((user.getFavoriteCount() != null ? user.getFavoriteCount() : 0) + 1);
            userRepository.updateById(user);
            System.out.println("用户收藏数更新: " + user.getUsername() + " 收藏数: " + user.getFavoriteCount());
        }
    }

    /**
     * 取消收藏猫咪
     */
    public void unfavoriteCat(Long catId) {
        // 获取当前用户ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByUsername(username);

        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 删除收藏记录
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("cat_id", catId)
                .eq("type", "CAT");

        favoriteRepository.delete(wrapper);
    }

    /**
     * 获取用户收藏的猫咪列表
     */
    public List<Cat> getUserFavoriteCats(Long userId) {
        // 查询用户收藏的猫咪ID列表
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("type", "CAT");

        List<Favorite> favorites = favoriteRepository.selectList(wrapper);

        // 获取猫咪详细信息
        return favorites.stream()
                .map(Favorite::getCatId)
                .map(catId -> catRepository.selectById(catId))
                .filter(cat -> cat != null)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否收藏了指定猫咪
     */
    public boolean isCatFavorited(Long catId) {
        // 获取当前用户ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByUsername(username);

        if (userId == null) {
            return false;
        }

        // 检查是否存在收藏记录
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("cat_id", catId)
                .eq("type", "CAT")
                .last("LIMIT 1");

        Favorite favorite = favoriteRepository.selectOne(wrapper);
        return favorite != null;
    }

    /**
     * 根据用户名获取用户ID
     */
    private Long getUserIdByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        // 如果username是User对象的字符串表示，直接获取ID
        if (username.startsWith("User")) {
            try {
                // 从认证信息中获取User对象
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof User) {
                    return ((User) principal).getId();
                }
            } catch (Exception e) {
                // 如果获取失败，回退到数据库查询
            }
        }

        // 回退到数据库查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .last("LIMIT 1");
        User user = userRepository.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}