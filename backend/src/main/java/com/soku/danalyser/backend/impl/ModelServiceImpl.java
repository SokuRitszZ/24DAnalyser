package com.soku.danalyser.backend.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.danalyser.backend.mapper.ModelMapper;
import com.soku.danalyser.backend.mapper.ModelPhotoMapper;
import com.soku.danalyser.backend.pojo.Model;
import com.soku.danalyser.backend.pojo.ModelPhoto;
import com.soku.danalyser.backend.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelServiceImpl implements ModelService {
  @Autowired private ModelMapper mapper;
  @Autowired private ModelPhotoMapper photoMapper;

  @Override
  public Map<String, String> add(Integer userId, String title) {
    Map<String, String> result = new HashMap<>();
    result.put("result", "fail");

    if (title == null || (title = title.trim()).length() == 0) {
      result.put("message", "标题为空");
      return result;
    }

    if (title.length() > 32) {
      result.put("message", "标题长度大于32");
      return result;
    }

    Model model = new Model(null, title, userId);
    mapper.insert(model);

    result.put("result", "success");
    result.put("id", "" + model.getId());

    return result;
  }

  @Override
  public Map<String, String> modify(Integer id, String title) {
    Map<String, String> result = new HashMap<>();
    result.put("result", "fail");

    if (title == null || (title = title.trim()).length() == 0) {
      result.put("message", "标题为空");
      return result;
    }

    if (title.length() > 32) {
      result.put("message", "标题长度大于32");
      return result;
    }

    Model model = mapper.selectById(id);
    model.setTitle(title);
    mapper.updateById(model);

    result.put("result", "success");
    result.put("title", title);
    return result;
  }

  @Override
  public Map<String, String> addPhoto(Integer id, MultipartFile photo, Double preview) {
    Map<String, String> result = new HashMap<>();
    result.put("result", "fail");
    try {
      byte[] bytes = photo.getBytes();
      ModelPhoto modelPhoto = new ModelPhoto(null, bytes, id, preview);
      photoMapper.insert(modelPhoto);
      result.put("result", "success");
      result.put("id", "" + modelPhoto.getId());
      return result;
    } catch (IOException e) {
      result.put("message", "无法存入数据库");
      return result;
    }
  }

  @Override
  public Map<String, String> modifyPhoto(Integer id, Double preview) {
    return null;
  }

  @Override
  public JSONObject get(Integer id) {
    JSONObject json = new JSONObject();
    List<Model> items = mapper.selectList(new QueryWrapper<Model>().eq("user_id", id));
    json.set("list", items);
    return json;
  }

  @Override
  public JSONObject getPhotos(Integer id) {
    JSONObject json = new JSONObject();
    List<ModelPhoto> items = photoMapper.selectList(new QueryWrapper<ModelPhoto>().eq("model_id", id));
    json.set("list", items);
    return json;
  }

  @Override
  public void removeModel(Integer id) {
    photoMapper.delete(new QueryWrapper<ModelPhoto>().eq("model_id", id));
    mapper.deleteById(id);
    return ;
  }

  @Override
  public void removePhoto(Integer id) {
    photoMapper.deleteById(id);
    return ;
  }
}
