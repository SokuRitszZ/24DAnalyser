package com.soku.danalyser.backend.service;

import java.util.Map;

public interface UserService {
  Map<String, String> register(String username, String password);
  Map<String, String> login(String username, String password);
}
