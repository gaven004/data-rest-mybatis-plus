# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.5/maven-plugin/reference/html/#build-image)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.7.5/reference/htmlsingle/#appendix.configuration-metadata.annotation-processor)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.5/reference/htmlsingle/#web)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### 实现分解

1. 实现自定义的HandlerMapping

### HandlerMapping生命周期

#### 1. 注册

DispatcherServlet中，initHandlerMappings()方法，查找已注册的HandlerMapping组件，即实现HandlerMapping接口的Bean，
并按order属性排序，存储于handlerMappings属性

``` java
// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
Map<String, HandlerMapping> matchingBeans =
        BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
if (!matchingBeans.isEmpty()) {
    this.handlerMappings = new ArrayList<>(matchingBeans.values());
    // We keep HandlerMappings in sorted order.
    AnnotationAwareOrderComparator.sort(this.handlerMappings);
}
```

#### 2. 匹配

DispatcherServlet处理HTTP请求，doDispatch()方法，查找对应的Handler，并进行调用

``` java
// Determine handler for the current request.
mappedHandler = getHandler(processedRequest);

// Tries all handler mappings in order.
for (HandlerMapping mapping : this.handlerMappings) {
    HandlerExecutionChain handler = mapping.getHandler(request);
    if (handler != null) {
        return handler;
    }
}
```

匹配算法是顺序调用各个HandlerMapping实现中重载的getHandler方法，按HTTP请求的URL、method等因子进行选择，首个匹配处理的方法则返回

留意AbstractHandlerMethodMapping.lookupHandlerMethod()方法，URL的匹配逻辑，如有通配符等情况上，如何匹配最为符合的处理方法

之后获取对应的HandlerAdapter

``` java
// Determine handler adapter for the current request.
HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
```

HandlerAdapter Interface that must be implemented for each handler type to handle a request. This interface is used to
allow the DispatcherServlet to be indefinitely extensible. The DispatcherServlet accesses all installed handlers through
this interface, meaning that it does not contain code specific to any handler type.

#### 3. 调用

doDispatch()方法中调用

``` java
// 预处理
if (!mappedHandler.applyPreHandle(processedRequest, response)) {
    return;
}

// Actually invoke the handler.
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

典型的调用，RequestMappingHandlerAdapter.invokeHandlerMethod()

``` java
ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
if (this.argumentResolvers != null) {
    invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
}
if (this.returnValueHandlers != null) {
    invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
}
invocableMethod.setDataBinderFactory(binderFactory);
invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

// ...

invocableMethod.invokeAndHandle(webRequest, mavContainer);
```
