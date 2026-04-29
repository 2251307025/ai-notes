package com.chj.tool;

import com.chj.pojo.User;
import com.chj.service.UserService;
import com.chj.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UserTool {
    @Resource
    private UserService userService;

    @Tool(description = "获取当前登录用户的信息")
    public User getCurrentUserInfo() {
        log.info("调用获取当前用户信息tool");
        Map<String, Object> map = ThreadLocalUtil.get();
        if (map != null) {
            String username = (String) map.get("username");
            if (username != null) {
                return userService.findByUserName(username);
            }
        }
        return null;
    }
}
