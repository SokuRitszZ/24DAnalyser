package com.soku.danalyser.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelPhoto {
  @TableId(type = IdType.AUTO)
  private Integer id;
  private byte[] photo;
  private Integer modelId;
  private Double preview;
}