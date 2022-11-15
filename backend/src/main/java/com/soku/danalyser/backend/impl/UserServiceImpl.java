package com.soku.danalyser.backend.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.danalyser.backend.mapper.UserMapper;
import com.soku.danalyser.backend.pojo.User;
import com.soku.danalyser.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
  @Autowired
  private UserMapper mapper;

  @Override
  public Map<String, String> register(String username, String password) {
    Map<String, String> result = new HashMap<>();
    result.put("result", "fail");

    if (username == null || username.trim().length() == 0) {
      result.put("message", "用户名为空");
      return result;
    }
    username = username.trim();
    if (username.length() > 16) {
      result.put("message", "用户名长度超过16");
      return result;
    }

    if (password == null || password.trim().length() == 0) {
      result.put("message", "密码为空");
      return result;
    }
    password = password.trim();
    if (password.length() > 32) {
      result.put("message", "密码长度超过32");
      return result;
    }

    User theNameUser = mapper.selectOne(new QueryWrapper<User>().eq("username", username));
    if (theNameUser != null) {
      result.put("message", "用户名已存在");
      return result;
    }

    User user = new User(null, username, password);
    mapper.insert(user);

    result.put("result", "success");
    return result;
  }

  @Override
  public Map<String, String> login(String username, String password) {
    Map<String, String> result = new HashMap<>();
    result.put("result", "fail");

    User user = mapper.selectOne(new QueryWrapper<User>().eq("username", username).eq("password", password));
    if (user == null) {
      result.put("message", "用户名或密码错误");
      return result;
    }

    result.put("result", "success");
    result.put("id", "" + user.getId());
    return result;
  }
}
