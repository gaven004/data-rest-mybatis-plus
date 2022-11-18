package com.g.data.rest;

import java.lang.reflect.Method;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility methods to work with requests and URIs.
 *
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public abstract class UriUtils {

    private UriUtils() {}

    /**
     * Returns the value for the mapping variable with the given name.
     *
     * @param variable must not be {@literal null} or empty.
     * @param method must not be {@literal null}.
     * @param lookupPath
     * @return
     */
    public static String findMappingVariable(String variable, Method method, String lookupPath) {

        Assert.hasText(variable, "Variable name must not be null or empty!");
        Assert.notNull(method, "Method must not be null!");

        String mapping = getMapping(method);

        return new org.springframework.web.util.UriTemplate(mapping) //
                .match(lookupPath) //
                .get(variable);
    }

    @Nullable
    private static String getMapping(Method method) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (requestMapping == null) {
            throw new IllegalArgumentException("No @RequestMapping on: " + method.toGenericString());
        }

        String[] paths = requestMapping.path();
        String mapping = paths.length == 0 ? null : paths[0];
        return mapping;
    }

    /**
     * Returns the mapping path segments request mapping.
     *
     * @param method must not be {@literal null}.
     * @return
     */
    public static List<String> getPathSegments(Method method) {

        Assert.notNull(method, "Method must not be null!");

//        String mapping = DISCOVERER.getMapping(method.getDeclaringClass(), method);
        String mapping = "";

        return UriComponentsBuilder.fromPath(mapping).build().getPathSegments();
    }
}
