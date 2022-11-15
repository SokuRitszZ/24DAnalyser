package com.soku.danalyser.backend.controller;

import com.soku.danalyser.backend.service.UserService;
import com.soku.danalyser.backend.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
  @Autowired
  private UserService service;

  @PostMapping("/register")
  public Map<String, String> registerController(@RequestParam Map<String, String> data) {
    String username = data.get("username");
    String password = data.get("password");
    return service.register(username, password);
  }

  @PostMapping("/login")
  public Map<String, String> loginController(@RequestParam Map<String, String> data) {
    String username = data.get("username");
    String password = data.get("password");
    return service.login(username, password);
  }
}
