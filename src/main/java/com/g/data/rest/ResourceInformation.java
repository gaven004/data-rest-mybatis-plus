package com.g.data.rest;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public class ResourceInformation {
    private String repository;

    private BaseMapper<?> mapper;

    private Class domainType;

    public ResourceInformation() {
    }

    public ResourceInformation(String repository, BaseMapper<?> mapper, Class domainType) {
        this.repository = repository;
        this.mapper = mapper;
        this.domainType = domainType;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public BaseMapper<?> getMapper() {
        return mapper;
    }

    public void setMapper(BaseMapper<?> mapper) {
        this.mapper = mapper;
    }

    public Class getDomainType() {
        return domainType;
    }

    public void setDomainType(Class domainType) {
        this.domainType = domainType;
    }
}
