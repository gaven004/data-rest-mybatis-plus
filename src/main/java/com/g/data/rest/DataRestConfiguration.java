package com.g.data.rest;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.XmlBeamHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class DataRestConfiguration {
    private final ApplicationContext context;

    public static final String BASE_URI = "_rest";

    private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


    public DataRestConfiguration(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    @Bean
    public BaseUri baseUri() {
        return new BaseUri(BASE_URI);
    }

    @Bean
    public RepositoryRestHandlerMapping restHandlerMapping() {
        return new RepositoryRestHandlerMapping(BASE_URI);
    }

    @Bean
    public ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver(
            ApplicationContext applicationContext, BaseUri baseUri) {
        return new ResourceInformationHandlerMethodArgumentResolver(applicationContext, baseUri);
    }

    /**
     * Special {@link org.springframework.web.servlet.HandlerAdapter} that only recognizes handler methods defined in the
     * provided controller classes.
     *
     * @return
     */
    @Bean
    public RequestMappingHandlerAdapter repositoryRestHandlerAdapter(
            ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver) {
        RepositoryRestHandlerAdapter handlerAdapter = new RepositoryRestHandlerAdapter(
                defaultMethodArgumentResolvers(repoRequestArgumentResolver));
        List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();
        extendMessageConverters(converters);
        return handlerAdapter;
    }

    protected List<HandlerMethodArgumentResolver> defaultMethodArgumentResolvers(
            ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver) {
        return Arrays.asList(
                new PageableHandlerMethodArgumentResolver(),
                new SortHandlerMethodArgumentResolver(),
                repoRequestArgumentResolver);
    }

    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        boolean jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", beanClassLoader) &&
                ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", beanClassLoader);
        boolean gsonPresent = ClassUtils.isPresent("com.google.gson.Gson", beanClassLoader);
        boolean jsonbPresent = ClassUtils.isPresent("javax.json.bind.Jsonb", beanClassLoader);

        if (jackson2Present) {
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            if (this.context != null) {
                builder.applicationContext(this.context);
            }
            converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        }
        else if (gsonPresent) {
            converters.add(new GsonHttpMessageConverter());
        }
        else if (jsonbPresent) {
            converters.add(new JsonbHttpMessageConverter());
        }
    }
}
