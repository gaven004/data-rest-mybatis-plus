package com.g.data.rest;

import java.io.Serializable;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

public class BackendIdsHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private final ResourceInformationHandlerMethodArgumentResolver repoResolver;
    private final BaseUri baseUri;

    public BackendIdsHandlerMethodArgumentResolver(ResourceInformationHandlerMethodArgumentResolver repoResolver,
                                                   BaseUri baseUri) {
        Assert.notNull(repoResolver, "ResourceInformationHandlerMethodArgumentResolver must not be null!");
        Assert.notNull(baseUri, "BaseUri must not be null!");

        this.repoResolver = repoResolver;
        this.baseUri = baseUri;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(BackendIds.class);
    }

    @Override
    public Serializable[] resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
            throws Exception {

        String lookupPath = baseUri.getRepositoryLookupPath(webRequest);

        String repositoryKey = UriUtils.findMappingVariable("repository", parameter.getMethod(), lookupPath);
        if (!StringUtils.hasText(repositoryKey)) {
            throw new IllegalArgumentException("Could not obtain repository for request " + webRequest);
        }

        String idSource = webRequest.getParameter("ids");
        if (!StringUtils.hasText(idSource)) {
            throw new IllegalArgumentException("Could not obtain id for request " + webRequest);
        }

        final ResourceMetadata metadata = repoResolver.getMetadata(repositoryKey);
        if (metadata == null) {
            throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
        }

        final TableInfo tableInfo = TableInfoHelper.getTableInfo(metadata.getDomainType());
        if (tableInfo == null) {
            throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
        }

        final Class<?> keyType = tableInfo.getKeyType();
        if (keyType == null) {
            throw new IllegalArgumentException(String.format("Could not resolve key type for %s.", repositoryKey));
        }

        final String[] idSources = idSource.split(",");

        if (keyType.isAssignableFrom(String.class)) {
            return idSources;
        }

        final ConversionService conversionService = DefaultConversionService.getSharedInstance();
        if (conversionService.canConvert(String.class, keyType)) {
            Serializable[] ids = new Serializable[idSources.length];
            for (int i = 0; i < idSources.length; i++) {
                ids[i] = (Serializable) conversionService.convert(idSources[i].trim(), keyType);
            }

            return ids;
        }

        return idSources;
    }
}
