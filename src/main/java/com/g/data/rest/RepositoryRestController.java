package com.g.data.rest;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Annotation to declare a controller that declares request mappings to be augmented with a base URI in the Spring Data
 * REST configuration.
 *
 * @author Oliver Gierke
 * @author Yves Galante
 */
@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
public @interface RepositoryRestController {
    /**
     * The root path to be prepended to all request mappings configured on handler methods.
     *
     * @return
     * @since 3.7.2
     * @see #path()
     */
    @AliasFor("path")
    String[] value() default {};

    /**
     * The root path to be prepended to all request mappings configured on handler methods.
     *
     * @return
     * @since 3.7.2
     * @see #value()
     */
    @AliasFor("value")
    String[] path() default {};
}
