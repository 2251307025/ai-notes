package com.chj.controller;


import com.chj.pojo.Result;
import com.chj.pojo.User;
import com.chj.service.UserService;
import com.chj.utils.JwtUtil;
import com.chj.utils.Md5Util;
import com.chj.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private StringRedisTemplate srt;
    @Autowired
    UserService userService;
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$")String password){
            log.info("请求路径: /user/register, username={}", username);
            User u= userService.findByUserName(username);
            if (u==null){
                userService.register(username,password);
                return Result.success();
            }else {
                return Result.error("用户名已被占用");
            }
    }
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$")String username,@Pattern(regexp = "^\\S{5,16}$")String password){
        log.info("请求路径: /user/login, username={}", username);
        User loginUser = userService.findByUserName(username);
        if (loginUser==null){
            return Result.error("用户名错误");
        }
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", loginUser.getId());
            map.put("username", loginUser.getUsername());
            String jwt = JwtUtil.getJwt(map);
            ValueOperations<String, String> sso = srt.opsForValue();
            sso.set(jwt, jwt, 1, TimeUnit.HOURS);
            return Result.success(jwt);
        }
        return Result.error("密码错误");
    }
    @GetMapping("/userInfo")
    public Result<User> userInfo(){
        log.info("请求路径: /user/userInfo");
        Map<String,Object> o = ThreadLocalUtil.get();
        User user = userService.findByUserName((String) o.get("username"));
        return Result.success(user);
    }
    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        log.info("请求路径: /user/update, user={}", user);
        userService.update(user);
        return Result.success();
    }
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        log.info("请求路径: /user/updateAvatar, avatarUrl={}", avatarUrl);
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        log.info("请求路径: /user/updatePwd");
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        if (!StringUtils.hasLength(oldPwd)||!StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要的参数");
        }
        Map<String,Object> map = ThreadLocalUtil.get();
        String u = (String) map.get("username");
        User user = userService.findByUserName(u);
        if (!user.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码填写不正确");
        }
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }
        userService.updatePwd(newPwd);
        ValueOperations<String, String> sso = srt.opsForValue();
        sso.getOperations().delete(token);
        return Result.success();
    }
}
