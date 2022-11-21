package com.g.data.rest;

import static org.springframework.util.StringUtils.hasText;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

public class PersistentEntityResourceHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String ERROR_MESSAGE = "Could not read an object of type %s from the request!";
    private static final String MISMATCH_ID = "Mismatch id value, [%s] from path, but [%s] in body!";
    private static final String NO_CONVERTER_FOUND = "No suitable HttpMessageConverter found to read request body into object of type %s from request with content type of %s!";

    private final List<HttpMessageConverter<?>> messageConverters;
    private final ResourceInformationHandlerMethodArgumentResolver repoResolver;
    private final BackendIdHandlerMethodArgumentResolver backendIdResolver;

    public PersistentEntityResourceHandlerMethodArgumentResolver(
            List<HttpMessageConverter<?>> messageConverters,
            ResourceInformationHandlerMethodArgumentResolver repoResolver,
            BackendIdHandlerMethodArgumentResolver backendIdResolver) {

        Assert.notNull(messageConverters, "HttpMessageConverters must not be null!");
        Assert.notNull(repoResolver, "ResourceInformationHandlerMethodArgumentResolver must not be null!");
        Assert.notNull(backendIdResolver, "BackendIdHandlerMethodArgumentResolver must not be null!");

        this.messageConverters = messageConverters;
        this.repoResolver = repoResolver;
        this.backendIdResolver = backendIdResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PersistentEntityResource.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String repositoryKey = repoResolver.getRepositoryKey(parameter, webRequest);
        if (!hasText(repositoryKey)) {
            throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
        }

        final ResourceMetadata metadata = repoResolver.getMetadata(repositoryKey);
        if (metadata == null) {
            throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
        }

        HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);

        Class<?> domainType = metadata.getDomainType();
        MediaType contentType = request.getHeaders().getContentType();

        for (HttpMessageConverter converter : messageConverters) {

            if (!converter.canRead(domainType, contentType)) {
                continue;
            }

            Serializable id = null;
            try {
                id = backendIdResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
            } catch (Exception e) {
                // Ignore
            }
            Object newObject = read(request, converter, metadata);

            if (newObject == null) {
                throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, domainType), request);
            }

            /**
             * 按规范，PATCH为部分更新，PUT全量更新，这里暂时不区别处理
             */
            if (request.getMethod().equals(HttpMethod.PATCH)
                    && converter instanceof MappingJackson2HttpMessageConverter) {
                // JSON + PATCH request
                return merge(newObject, domainType, id);
            } else if (request.getMethod().equals(HttpMethod.PUT)
                    && converter instanceof MappingJackson2HttpMessageConverter) {
                // JSON + PUT request
                return merge(newObject, domainType, id);
            }

            return newObject;
        }

        throw new HttpMessageNotReadableException(String.format(NO_CONVERTER_FOUND, domainType, contentType), request);
    }

    private Object read(ServletServerHttpRequest request, HttpMessageConverter<Object> converter,
                        ResourceMetadata metadata) {

        try {
            return converter.read(metadata.getDomainType(), request);
        } catch (IOException o_O) {
            throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, metadata.getDomainType()), o_O,
                    request);
        }
    }

    private Object merge(Object newObject, Class<?> domainType, Serializable id)
            throws InvocationTargetException, IllegalAccessException {

        final TableInfo tableInfo = TableInfoHelper.getTableInfo(domainType);
        final String keyProperty = tableInfo.getKeyProperty();
        final PropertyDescriptor keyPD = BeanUtils.getPropertyDescriptor(domainType, keyProperty);
        final Method readMethod = keyPD.getReadMethod();
        final Object srcId = readMethod.invoke(newObject, null);

        if (srcId != null) {
            if (!srcId.equals(id)) {
                throw new IllegalArgumentException(String.format(MISMATCH_ID, id, srcId));
            }
        } else {
            final Method writeMethod = keyPD.getWriteMethod();
            writeMethod.invoke(newObject, id);
        }

        return newObject;
    }
}
