package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.entity.Favorite;
import com.catrescue.catrescueplatform.service.CatFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cats")
@RequiredArgsConstructor
public class CatFavoriteController {

    private final CatFavoriteService catFavoriteService;

    /**
     * 收藏猫咪
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> favoriteCat(@PathVariable Long id) {
        catFavoriteService.favoriteCat(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消收藏猫咪
     */
    @PostMapping("/{id}/unfavorite")
    public ResponseEntity<?> unfavoriteCat(@PathVariable Long id) {
        catFavoriteService.unfavoriteCat(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户收藏的猫咪列表
     */
    @GetMapping("/users/{userId}/favorites")
    public ResponseEntity<List<Cat>> getUserFavoriteCats(@PathVariable Long userId) {
        List<Cat> favoriteCats = catFavoriteService.getUserFavoriteCats(userId);
        return ResponseEntity.ok(favoriteCats);
    }

    /**
     * 检查用户是否收藏了指定猫咪
     */
    @GetMapping("/{id}/favorite/status")
    public ResponseEntity<Boolean> checkFavoriteStatus(@PathVariable Long id) {
        boolean isFavorited = catFavoriteService.isCatFavorited(id);
        return ResponseEntity.ok(isFavorited);
    }
}