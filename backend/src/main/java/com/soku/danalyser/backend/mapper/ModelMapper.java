package com.soku.danalyser.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.danalyser.backend.pojo.Model;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ModelMapper extends BaseMapper<Model> {
}
