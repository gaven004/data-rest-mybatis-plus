package com.g.data.rest;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * {@link RequestMappingHandlerAdapter} implementation that adds a couple argument resolvers for controller method
 * parameters used in the REST exporter controller. Also only looks for handler methods in the Spring Data REST provided
 * controller classes to help isolate this handler adapter from other handler adapters the user might have configured in
 * their Spring MVC context.
 *
 * @author Jon Brisbin
 * @author Oliver Gierke
 */
public class RepositoryRestHandlerAdapter extends RequestMappingHandlerAdapter {

    private final List<HandlerMethodArgumentResolver> argumentResolvers;

    /**
     * Creates a new {@link RepositoryRestHandlerAdapter} using the given {@link HandlerMethodArgumentResolver}s.
     *
     * @param argumentResolvers must not be {@literal null}.
     */
    public RepositoryRestHandlerAdapter(List<HandlerMethodArgumentResolver> argumentResolvers) {

        this.argumentResolvers = argumentResolvers;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.rest.webmvc.RepresentationModelProcessorInvokingHandlerAdapter#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        setCustomArgumentResolvers(argumentResolvers);
        super.afterPropertiesSet();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter#getOrder()
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#supportsInternal(org.springframework.web.method.HandlerMethod)
     */
    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {

        Class<?> controllerType = handlerMethod.getBeanType();

        return AnnotationUtils.findAnnotation(controllerType, RepositoryRestController.class) != null;
    }
}
