package com.chj.service.impl;


import com.chj.anno.AutoFill;
import com.chj.mapper.UserMapper;
import com.chj.pojo.User;
import com.chj.service.UserService;
import com.chj.utils.Md5Util;
import com.chj.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl  implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public User findByUserName(String username) {
        User u= userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
        //密码要加密
        String md5String = Md5Util.getMD5String(password);
        userMapper.register(username,md5String);
    }

    @Override
    @AutoFill(AutoFill.OperationType.UPDATE)
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> o = ThreadLocalUtil.get();
        Integer id = (Integer) o.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }
}
