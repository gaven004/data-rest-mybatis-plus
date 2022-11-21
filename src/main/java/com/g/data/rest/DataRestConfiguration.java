package com.g.data.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

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

    @Bean
    public MybatisPlusPageHandlerMethodArgumentResolver pageRequestArgumentResolver(
            ResourceInformationHandlerMethodArgumentResolver repoResolver,
            PageableHandlerMethodArgumentResolver pageableResolver,
            SortHandlerMethodArgumentResolver sortResolver) {
        return new MybatisPlusPageHandlerMethodArgumentResolver(repoResolver, pageableResolver, sortResolver);
    }

    @Bean
    public BackendIdHandlerMethodArgumentResolver backendIdArgumentResolver(
            ResourceInformationHandlerMethodArgumentResolver repoResolver, BaseUri baseUri) {
        return new BackendIdHandlerMethodArgumentResolver(repoResolver, baseUri);
    }

    @Bean
    public BackendIdsHandlerMethodArgumentResolver backendIdsArgumentResolver(
            ResourceInformationHandlerMethodArgumentResolver repoResolver, BaseUri baseUri) {
        return new BackendIdsHandlerMethodArgumentResolver(repoResolver, baseUri);
    }

    /**
     * Reads incoming JSON into an entity.
     *
     * @return
     */
    @Bean
    public PersistentEntityResourceHandlerMethodArgumentResolver persistentEntityArgumentResolver(
            ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver,
            BackendIdHandlerMethodArgumentResolver backendIdArgumentResolver) {

        List<HttpMessageConverter<?>> defaultMessageConverters = new ArrayList<>(1);
        extendMessageConverters(defaultMessageConverters);

        return new PersistentEntityResourceHandlerMethodArgumentResolver(defaultMessageConverters,
                repoRequestArgumentResolver, backendIdArgumentResolver);
    }

    /**
     * Special {@link org.springframework.web.servlet.HandlerAdapter} that only recognizes handler methods defined in the
     * provided controller classes.
     *
     * @return
     */
    @Bean
    public RequestMappingHandlerAdapter repositoryRestHandlerAdapter(
            ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver,
            MybatisPlusPageHandlerMethodArgumentResolver pageRequestArgumentResolver,
            BackendIdHandlerMethodArgumentResolver backendIdArgumentResolver,
            BackendIdsHandlerMethodArgumentResolver backendIdsArgumentResolver,
            PersistentEntityResourceHandlerMethodArgumentResolver persistentEntityArgumentResolver) {

        RepositoryRestHandlerAdapter handlerAdapter = new RepositoryRestHandlerAdapter(
                defaultMethodArgumentResolvers(repoRequestArgumentResolver, pageRequestArgumentResolver,
                        backendIdArgumentResolver, backendIdsArgumentResolver, persistentEntityArgumentResolver));
        List<HttpMessageConverter<?>> converters = handlerAdapter.getMessageConverters();
        extendMessageConverters(converters);
        return handlerAdapter;
    }

    protected List<HandlerMethodArgumentResolver> defaultMethodArgumentResolvers(
            ResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver,
            MybatisPlusPageHandlerMethodArgumentResolver pageRequestArgumentResolver,
            BackendIdHandlerMethodArgumentResolver backendIdArgumentResolver,
            BackendIdsHandlerMethodArgumentResolver backendIdsArgumentResolver,
            PersistentEntityResourceHandlerMethodArgumentResolver persistentEntityArgumentResolver) {

        return Arrays.asList(pageRequestArgumentResolver, repoRequestArgumentResolver,
                backendIdArgumentResolver, backendIdsArgumentResolver, persistentEntityArgumentResolver);
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
        } else if (gsonPresent) {
            converters.add(new GsonHttpMessageConverter());
        } else if (jsonbPresent) {
            converters.add(new JsonbHttpMessageConverter());
        }
    }
}
