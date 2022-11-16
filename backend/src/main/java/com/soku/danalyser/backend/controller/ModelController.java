package com.soku.danalyser.backend.controller;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mysql.cj.xdevapi.JsonArray;
import com.soku.danalyser.backend.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequestMapping("/model")
@RestController
public class ModelController {
  @Autowired
  private ModelService service;

  @PostMapping("/add")
  public Map<String, String> add(@RequestParam Map<String, String> data) {
    Integer userId = Integer.parseInt(data.get("userId"));
    String title = data.get("title");
    return service.add(userId, title);
  }

  @PostMapping("/modify")
  public Map<String, String> modify(@RequestParam Map<String, String> data) {
    Integer id = Integer.parseInt(data.get("id"));
    String title = data.get("title");
    return service.modify(id, title);
  }

  @PostMapping("/addPhoto")
  public Map<String, String> addPhoto(@RequestParam("file")MultipartFile file, @RequestParam("id") Integer id, @RequestParam("preview") Double preview) {
    return service.addPhoto(id, file, preview);
  }

  @PostMapping("/modifyPhoto")
  public Map<String, String> modifyPhoto(@RequestParam Map<String, String> data) {
    Integer id = Integer.parseInt(data.get("id"));
    Double preview = Double.parseDouble(data.get("preview"));
    return service.modifyPhoto(id, preview);
  }

  @PostMapping("/get")
  public JSONObject get(@RequestParam Map<String, String> data) {
    Integer id = Integer.parseInt(data.get("id"));
    return service.get(id);
  }

  @PostMapping("/getPhotos")
  public JSONObject getPhotos(@RequestParam Map<String, String> data) {
    Integer id = Integer.parseInt(data.get("id"));
    return service.getPhotos(id);
  }
}