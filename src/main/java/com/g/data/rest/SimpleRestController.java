package com.g.data.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleRestController {
    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public ApiResponse hello(String name, int i) {
        return ApiResponse.success("Hello");
    }
}
