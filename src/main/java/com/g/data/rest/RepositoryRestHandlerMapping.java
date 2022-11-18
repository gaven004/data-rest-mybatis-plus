package com.g.data.rest;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.util.ProxyUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class RepositoryRestHandlerMapping extends RequestMappingHandlerMapping {
    private static final String AT_REQUEST_MAPPING_ON_TYPE = "Spring Data REST controller %s must not use @RequestMapping on class level as this would cause double registration with Spring MVC!";

    private final String baseUri;

    public RepositoryRestHandlerMapping(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    @SuppressWarnings("null")
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {

        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        if (info == null) {
            return null;
        }

        ProducesRequestCondition producesCondition = customize(info.getProducesCondition());
        Set<MediaType> mediaTypes = producesCondition.getProducibleMediaTypes();
        String[] customPrefixes = getBasePathedPrefixes(handlerType);
        RequestMappingInfo.Builder builder = info.mutate();

        if ((customPrefixes.length != 0) || StringUtils.hasText(baseUri)) {
            builder = builder.paths(resolveEmbeddedValuesInPatterns(customPrefixes));
        }

        return builder //
                .produces(mediaTypes.stream().map(MediaType::toString).toArray(String[]::new)) //
                .build() //
                .combine(info);
    }

    /**
     * Customize the given {@link ProducesRequestCondition}. Default implementation returns the condition as is.
     *
     * @param condition will never be {@literal null}.
     * @return
     */
    protected ProducesRequestCondition customize(ProducesRequestCondition condition) {
        return condition;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {

        Class<?> type = ProxyUtils.getUserClass(beanType);
        boolean isSpringDataRestHandler = isHandlerInternal(type);

        if (!isSpringDataRestHandler) {
            return false;
        }

        if (AnnotatedElementUtils.hasAnnotation(type, RequestMapping.class)) {
            throw new IllegalStateException(String.format(AT_REQUEST_MAPPING_ON_TYPE, beanType.getName()));
        }

        return isSpringDataRestHandler;
    }

    protected boolean isHandlerInternal(Class<?> type) {
        return AnnotatedElementUtils.hasAnnotation(type, RepositoryRestController.class);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    private String[] getBasePathedPrefixes(Class<?> handlerType) {

        Assert.notNull(handlerType, "Handler type must not be null");

        RepositoryRestController mergedAnnotation = findMergedAnnotation(handlerType, RepositoryRestController.class);
        String[] customPrefixes = mergedAnnotation == null ? new String[0] : mergedAnnotation.value();

        return customPrefixes.length == 0 //
                ? new String[] { baseUri } //
                : Arrays.stream(customPrefixes).map(baseUri::concat).toArray(String[]::new);
    }
}
