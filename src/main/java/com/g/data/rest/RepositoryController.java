package com.g.data.rest;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

@RepositoryRestController
public class RepositoryController {
    private static final String BASE_MAPPING = "/{repository}";

    @ResponseBody
    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    public HttpEntity<?> index() {
        return new ResponseEntity<>("Repository Rest Controller", HttpStatus.OK);
    }

    /**
     * <code>GET /{repository}</code> - Returns the collection resource (paged or unpaged).
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.GET)
    public IPage<?> getCollectionResource(ResourceInformation resourceInformation, IPage pageable)
            throws ResourceNotFoundException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        final Wrapper wrapper = resourceInformation.getWrapper();
        return mapper.selectPage(pageable, wrapper);
    }

    /**
     * <code>POST /{repository}</code> - Creates a new entity instances from the collection resource.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.POST)
    public ResponseEntity<?> postCollectionResource(ResourceInformation resourceInformation,
                                                    @PersistentEntityResource Object payload)
            throws HttpRequestMethodNotSupportedException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        return createAndReturn(payload, mapper);
    }

    /**
     * <code>DELETE /{repository}?ids=recordId,recordId</code> - Deletes the entity backing the item resource.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCollectionResource(ResourceInformation resourceInformation,
                                                      @BackendIds Serializable[] ids)
            throws HttpRequestMethodNotSupportedException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        for (Serializable id : ids) {
            mapper.deleteById(id);
        }

        return toEmptyResponse(HttpStatus.NO_CONTENT);
    }

    /**
     * <code>GET /{repository}/{id}</code> - Returns a single entity.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.GET)
    public Object getItemResource(ResourceInformation resourceInformation,
                                  @BackendId Serializable id)
            throws ResourceNotFoundException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        final Object entity = mapper.selectById(id);

        if (null == entity) {
            throw new ResourceNotFoundException();
        }

        return entity;
    }

    /**
     * <code>PUT /{repository}/{id}</code> - Updates an existing entity or creates one at exactly that place.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putItemResource(ResourceInformation resourceInformation,
                                             @BackendId Serializable id,
                                             @PersistentEntityResource Object payload)
            throws HttpRequestMethodNotSupportedException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        final Object entity = mapper.selectById(id);

        if (null == entity) {
            return createAndReturn(payload, mapper);
        }

        return saveAndReturn(payload, mapper);
    }

    /**
     * <code>PATCH /{repository}/{id}</code> - Updates an existing entity or creates one at exactly that place.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<?> patchItemResource(ResourceInformation resourceInformation,
                                               @PersistentEntityResource Object payload)
            throws HttpRequestMethodNotSupportedException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        return saveAndReturn(payload, mapper);
    }

    /**
     * <code>DELETE /{repository}/{id}</code> - Deletes the entity backing the item resource.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteItemResource(ResourceInformation resourceInformation,
                                                @BackendId Serializable id)
            throws HttpRequestMethodNotSupportedException {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        int rows = mapper.deleteById(id);
        if (rows > 0) {
            return toEmptyResponse(HttpStatus.NO_CONTENT);
        } else {
            return toEmptyResponse(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Merges the given incoming object into the given domain object.
     */
    private ResponseEntity<?> saveAndReturn(Object domainObject, BaseMapper invoker) {
        int rows = invoker.updateById(domainObject);
        if (rows > 0) {
            return toResponseEntity(HttpStatus.OK, null, Optional.of(domainObject));
        } else {
            return toEmptyResponse(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Triggers the creation of the domain object and renders it into the response if needed.
     */
    private ResponseEntity<?> createAndReturn(Object domainObject, BaseMapper invoker) {
        int rows = invoker.insert(domainObject);
        if (rows > 0) {
            return toResponseEntity(HttpStatus.CREATED, null, Optional.of(domainObject));
        } else {
            return toEmptyResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Wrap a resource as a {@link ResponseEntity} and attach given headers and status.
     *
     * @param status
     * @param headers
     * @param resource
     * @return
     */
    static <T> ResponseEntity<T> toResponseEntity(
            HttpStatus status, HttpHeaders headers, Optional<T> resource) {

        HttpHeaders hdrs = new HttpHeaders();

        if (headers != null) {
            hdrs.putAll(headers);
        }

        return new ResponseEntity<T>(resource.orElse(null), hdrs, status);
    }


    /**
     * Wrap a resource as a {@link ResponseEntity} and attach given headers and status.
     *
     * @param status
     * @param headers
     * @param resource
     * @return
     */
    static <T> ResponseEntity<T> toResponseEntity(
            HttpStatus status, HttpHeaders headers, T resource) {

        Assert.notNull(status, "Http status must not be null!");
        Assert.notNull(headers, "Http headers must not be null!");
        Assert.notNull(resource, "Payload must not be null!");

        return toResponseEntity(status, headers, Optional.of(resource));
    }

    /**
     * Return an empty response that is only comprised of a status
     *
     * @param status
     * @return
     */
    static ResponseEntity<?> toEmptyResponse(HttpStatus status) {
        return toEmptyResponse(status, new HttpHeaders());
    }

    /**
     * Return an empty response that is only comprised of headers and a status
     *
     * @param status
     * @param headers
     * @return
     */
    static ResponseEntity<?> toEmptyResponse(HttpStatus status, HttpHeaders headers) {
        return toResponseEntity(status, headers, Optional.empty());
    }
}
