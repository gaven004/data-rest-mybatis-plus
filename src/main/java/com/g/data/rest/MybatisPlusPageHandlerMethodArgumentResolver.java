package com.g.data.rest;

import static org.springframework.util.StringUtils.hasText;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baomidou.mybatisplus.core.metadata.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class MybatisPlusPageHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private final ApplicationContext applicationContext;
    private final BaseUri baseUri;
    private final ResourceInformationHandlerMethodArgumentResolver repoResolver;
    private final PageableHandlerMethodArgumentResolver pageableResolver;
    private final SortHandlerMethodArgumentResolver sortResolver;

    public MybatisPlusPageHandlerMethodArgumentResolver(ApplicationContext applicationContext,
                                                        BaseUri baseUri,
                                                        ResourceInformationHandlerMethodArgumentResolver repoResolver,
                                                        PageableHandlerMethodArgumentResolver pageableResolver,
                                                        SortHandlerMethodArgumentResolver sortResolver) {
        Assert.notNull(applicationContext, "ApplicationContext must not be null!");
        Assert.notNull(baseUri, "BaseUri must not be null!");
        Assert.notNull(repoResolver, "ResourceInformationHandlerMethodArgumentResolver must not be null!");
        Assert.notNull(pageableResolver, "PageableHandlerMethodArgumentResolver must not be null!");
        Assert.notNull(sortResolver, "SortHandlerMethodArgumentResolver must not be null!");

        this.applicationContext = applicationContext;
        this.baseUri = baseUri;
        this.repoResolver = repoResolver;
        this.pageableResolver = pageableResolver;
        this.sortResolver = sortResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return IPage.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public IPage resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String repositoryKey = repoResolver.getRepositoryKey(parameter, webRequest);
        if (!hasText(repositoryKey)) {
            throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
        }

        final Pageable pageable = pageableResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        final Sort sort = sortResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        Page page = new Page<>();

        if (pageable.isPaged()) {
            page.setCurrent(pageable.getPageNumber() == 0 ? 1 : pageable.getPageNumber());
            page.setSize(pageable.getPageSize());
        }

        if (sort.isSorted()) {
            final ResourceMetadata metadata = repoResolver.getMetadata(repositoryKey);
            if (metadata == null) {
                throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
            }

            final TableInfo tableInfo = TableInfoHelper.getTableInfo(metadata.getDomainType());
            if (tableInfo == null) {
                throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryKey));
            }

            final List<TableFieldInfo> fieldList = tableInfo.getFieldList();

            sort.forEach(order -> {
                String column = getMappingColumn(fieldList, order.getProperty());
                if (null != column) {
                    page.addOrder(new OrderItem(column, order.isAscending()));
                }
            });
        }

        return page;
    }

    private String getMappingColumn(List<TableFieldInfo> fieldList, String property) {
        for (TableFieldInfo fieldInfo : fieldList) {
            if (fieldInfo.getProperty().equals(property)) {
                return fieldInfo.getColumn();
            }
        }
        return null;
    }
}
