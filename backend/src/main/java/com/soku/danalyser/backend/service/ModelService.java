package com.soku.danalyser.backend.service;

import cn.hutool.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ModelService {
  Map<String, String> add(Integer userId, String title);

  Map<String, String> modify(Integer id, String title);

  Map<String, String> addPhoto(Integer id, MultipartFile photo, Double preview);

  Map<String, String> modifyPhoto(Integer id, Double preview);

  JSONObject get(Integer id);

  JSONObject getPhotos(Integer id);

  void removeModel(Integer id);

  void removePhoto(Integer id);
}
