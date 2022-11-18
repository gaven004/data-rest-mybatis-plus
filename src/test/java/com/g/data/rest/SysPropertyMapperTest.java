package com.g.data.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SysPropertyMapperTest {
    @Autowired
    SysPropertyMapper mapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        final List<SysProperty> list = mapper.selectList(null);
        list.forEach(System.out::println);

    }
}
