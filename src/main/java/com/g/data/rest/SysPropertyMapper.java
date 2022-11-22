package com.g.data.rest;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface SysPropertyMapper extends BaseMapper<SysProperty> {
    @Select("SELECT t.* FROM sys_properties t WHERE category = #{category} and status = 'VALID' ORDER BY sort_order")
    IPage<SysProperty> selectByCategory(IPage page, String category);
}
