package com.g.data.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

@RepositoryRestController
public class RepositorySearchController {
    private static final String SEARCH = "/search";

    private static final String BASE_MAPPING = "/{repository}" + SEARCH;

    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Executes the search with the given name.
     */
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING + "/{search}", method = RequestMethod.GET)
    public ResponseEntity<?> executeSearch(ResourceInformation resourceInformation,
                                           @RequestParam MultiValueMap<String, Object> parameters,
                                           @PathVariable String search,
                                           IPage pageable) {
        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        final Method method = checkExecutability(resourceInformation, search);
        Object[] args = getMethodArgumentValues(method, parameters, pageable);

        try {
            final Object result = method.invoke(mapper, args);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvocationTargetException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the method argument values for the current request, checking the provided argument values and
     * falling back to the configured argument resolvers.
     * The resulting array will be passed into doInvoke.
     */
    private Object[] getMethodArgumentValues(Method method, MultiValueMap<String, Object> valueMap, IPage pageable) {
        final Parameter[] parameters = method.getParameters();
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        final ConversionService conversionService = DefaultConversionService.getSharedInstance();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final String name = parameter.getName();
            final Class<?> type = parameter.getType();

            if (type.isAssignableFrom(IPage.class)) {
                args[i] = pageable;
            } else {
                final List<Object> paramValues = valueMap.get(name);
                args[i] = paramValues.size() == 1 ? paramValues.get(0) : paramValues;

                if (conversionService.canConvert(args[i].getClass(), type)) {
                    args[i] = conversionService.convert(args[i], type);
                }
            }
        }
        return args;
    }

    /**
     * Checks that the given request is actually executable. Will reject execution if we don't find a search with the
     * given name.
     *
     * @param resourceInformation
     * @param searchName
     * @return
     */
    private Method checkExecutability(ResourceInformation resourceInformation, String searchName) {

        final BaseMapper<?> mapper = resourceInformation.getMetadata().getMapper();
        final Class<? extends BaseMapper> mapperClass = mapper.getClass();

        // Get mapper interface
        final Class[] interfaces = mapperClass.getInterfaces();
        final Class mapperInterface = interfaces[0];

        Method method = BeanUtils.findMethodWithMinimalParameters(mapperInterface, searchName);

        if (method == null) {
            throw new ResourceNotFoundException();
        }

        return method;
    }

}
