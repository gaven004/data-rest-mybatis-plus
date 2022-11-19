package com.g.data.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@RepositoryRestController
public class RepositoryController {
    private static final String BASE_MAPPING = "/{repository}";

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    public HttpEntity<?> index() {
        return new ResponseEntity<>("Hello World", HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.GET)
    public Iterable<?> getCollectionResource(ResourceInformation resourceInformation,
                                             Pageable pageable)
            throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
        if (null == resourceInformation) {
            throw new ResourceNotFoundException();
        }

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        return mapper.selectList(null);
    }
}
