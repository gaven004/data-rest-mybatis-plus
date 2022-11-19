package com.g.data.rest;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

public class ResourceInformation {
    private ResourceMetadata metadata;

    private Wrapper wrapper;

    public ResourceInformation() {
    }

    public ResourceInformation(ResourceMetadata metadata, Wrapper wrapper) {
        this.metadata = metadata;
        this.wrapper = wrapper;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }
}
