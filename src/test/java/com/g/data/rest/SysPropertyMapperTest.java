package com.g.data.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@SpringBootTest
class SysPropertyMapperTest {
    @Autowired
    SysPropertyMapper mapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<SysProperty> list = mapper.selectList(null);
        list.forEach(System.out::println);

        System.out.println(("----- select method test ------"));
        SysProperty entity = new SysProperty();
//        entity.setSortOrder(0);
//        entity.setCategory("FILE");
//        entity.setStatus("VALID");
        Wrapper wrapper = new QueryWrapper(entity);
        list = mapper.selectList(wrapper);
        list.forEach(System.out::println);
    }
}
