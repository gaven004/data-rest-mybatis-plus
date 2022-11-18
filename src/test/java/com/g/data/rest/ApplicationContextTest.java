package com.g.data.rest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@SpringBootTest
public class ApplicationContextTest {
    @Autowired
    ApplicationContext context;

    @Autowired
    SqlSessionFactory sessionFactory;

    @Test
    void testContext() {
        final String[] beanNamesForType = context.getBeanNamesForType(BaseMapper.class, true, false);
        System.out.println("beanNamesForType = " + beanNamesForType);

        for (String name : beanNamesForType) {
            System.out.println("name = " + name);

            BaseMapper mapper = context.getBean(name, BaseMapper.class);
            System.out.println("mapper = " + mapper);


            Class<? extends BaseMapper> mapperClass = mapper.getClass();

            Type[] interfaces = mapperClass.getGenericInterfaces();
            Type mapperInterface = interfaces[0];
            System.out.println("mapperInterface = " + mapperInterface);

            ((Class) mapperInterface).getSimpleName();

            interfaces = ((Class) mapperInterface).getGenericInterfaces();
            Type baseMapperInterface = interfaces[0];
            System.out.println("baseMapperInterface = " + baseMapperInterface);

            if (baseMapperInterface instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) baseMapperInterface;
                final Type[] actualTypeArguments = type.getActualTypeArguments();
                System.out.println("actualTypeArguments = " + actualTypeArguments[0]);
            }

            final List list = mapper.selectList(null);
            list.forEach(System.out::println);
        }
    }

    @Test
    void testSessionFactory() {
        final Configuration configuration = sessionFactory.getConfiguration();
    }
}
