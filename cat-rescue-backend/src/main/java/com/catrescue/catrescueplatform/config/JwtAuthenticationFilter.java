package com.catrescue.catrescueplatform.config;

import com.catrescue.catrescueplatform.util.JwtUtil;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, javax.servlet.FilterChain chain)
            throws javax.servlet.ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {
                // 获取用户详细信息
                User user = userService.findByUsername(username);
                if (user != null) {
                    // 创建权限列表
                    List<GrantedAuthority> authorities = new ArrayList<>();

                    // 根据用户角色添加权限
                    if (user.getRole() != null) {
                        String role = user.getRole().toUpperCase();
                        if (role.equals("ADMIN")) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        } else if (role.equals("RESCUER")) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_RESCUER"));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                    } else {
                        // 默认用户角色
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        chain.doFilter(request, response);
    }
}