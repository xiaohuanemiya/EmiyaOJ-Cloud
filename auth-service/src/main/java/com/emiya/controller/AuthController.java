package com.emiya.controller;


import com.emiya.common.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin // 允许跨域
public class AuthController {

    private final IUserService userService;
    private final RedisUtil redisUtil;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseResult<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO, HttpServletRequest request){
        log.info("登录请求: {}", loginDTO);
        
        // 检查请求头中是否已存在token
        String existingToken = request.getHeader("Authorization");
        if (!ObjectUtils.isEmpty(existingToken)) {
            // 去掉前缀 "Bearer "
            if (existingToken.startsWith("Bearer ")) {
                existingToken = existingToken.substring(7);
            }

            // 验证现有token是否仍然有效
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), existingToken);
                String loginUserString = claims.get(JwtClaimsConstant.USER_LOGIN).toString();
                UserLogin existingUserLogin = JSON.parseObject(loginUserString, UserLogin.class);

                // 检查Redis中是否存在该token
                if (redisUtil.hasKey("token_" + existingUserLogin.getUser().getId())) {
                    // 如果现有token仍然有效，可以直接返回或提示用户
                    log.warn("用户 {} 尝试登录，但已有有效token", existingUserLogin.getUser().getId());
                    return ResponseResult.fail(409, "用户已登录");
                }
            } catch (Exception ignored) {
            }
        }

        UserLoginVO loginVO = userService.login(loginDTO);
        return ResponseResult.success(loginVO);
    }

    /**
     * 员工退出登录
     * @return  统一返回结果
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseResult<?> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("员工ID：{}，退出登录", BaseContext.getCurrentId());

        String token = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(token)) { // header没有token
            token = request.getParameter("Authorization");
        }
        
        // 去掉前缀 "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // 清除上下文
            new SecurityContextLogoutHandler().logout(request, response, authentication);
//            // 清理redis
//            redisUtil.delete("token_" + token);
            
            // 如果有token，则从Redis中删除对应用户ID的token记录
            if (!ObjectUtils.isEmpty(token)) {
                try {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                    String loginUserString = claims.get(JwtClaimsConstant.USER_LOGIN).toString();
                    UserLogin userLogin = JSON.parseObject(loginUserString, UserLogin.class);
                    // 使用用户ID作为key删除token
                    redisUtil.delete("token_" + userLogin.getUser().getId());
                } catch (Exception e) {
                    log.warn("Failed to parse token during logout: {}", e.getMessage());
                }
            }
            
            // 清理ThreadLocal
            BaseContext.remove();

        }
        return ResponseResult.success();
    }
}
