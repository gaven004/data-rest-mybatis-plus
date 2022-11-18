package com.g.data.rest;

import static org.springframework.util.StringUtils.hasText;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public class ResourceInformationHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private final ApplicationContext applicationContext;
    private final BaseUri baseUri;

    private final ConcurrentLruCache<String, ResourceInformation> resourceInformationCache = new ConcurrentLruCache<>(256,
            this::getResourceInformation);

    public ResourceInformationHandlerMethodArgumentResolver(
            ApplicationContext applicationContext, BaseUri baseUri) {
        Assert.notNull(applicationContext, "ApplicationContext must not be null!");
        Assert.notNull(baseUri, "BaseUri must not be null!");

        this.applicationContext = applicationContext;
        this.baseUri = baseUri;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ResourceInformation.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public ResourceInformation resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                               NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
            throws Exception {
        String lookupPath = baseUri.getRepositoryLookupPath(webRequest);
        String repositoryKey = UriUtils.findMappingVariable("repository", parameter.getMethod(), lookupPath);

        if (!hasText(repositoryKey)) {
            return null;
        }

        ResourceInformation ri = resourceInformationCache.get(repositoryKey);
        if (ri != null) {
            return ri;
        }

        throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
    }

    private ResourceInformation getResourceInformation(String key) {
        String mapperName = key + "Mapper";

        // Find by name
        BaseMapper mapper = applicationContext.getBean(mapperName, BaseMapper.class);

        if (null == mapper) {
            // Find by type
//            ((Class) mapperInterface).getSimpleName();
        }

        if (null == mapper) {
            return null;
        }

        Class<? extends BaseMapper> mapperClass = mapper.getClass();

        // Get mapper interface
        Type[] interfaces = mapperClass.getGenericInterfaces();
        Type mapperInterface = interfaces[0];

        // Get BaseMapper interface
        interfaces = ((Class) mapperInterface).getGenericInterfaces();
        Type baseMapperInterface = interfaces[0];

        // Get domain type from BaseMapper interface
        Class domainType = null;
        if (baseMapperInterface instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) baseMapperInterface;
            Type[] actualTypeArguments = type.getActualTypeArguments();
            domainType = (Class) actualTypeArguments[0];
        }

        return new ResourceInformation(key, mapper, domainType);
    }
}
