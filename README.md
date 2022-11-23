# Data Rest

参照Spring Data Rest，一个基于Mybatis Plus的自定义实现，支持简单的CRUD REST接口

Mybatis Plus中，Mapper是继承BaseMapper，如：interface SysPropertyMapper extends BaseMapper<SysProperty>

### 基础CRUD

REST接口的调用：
``` http request
GET /_rest/sysProperty
```
匹配对应的SysPropertyMapper，调用SysPropertyMapper的selectList()

其它增删方法：

``` http request
### POST SysProperty
POST http://localhost:8080/_rest/sysProperty
Accept: application/json
Content-Type: application/json

{
  "id": 359040721228922990,
  "category": "FILE_TYPE",
  "name": "PDF",
  "value": "PDF",
  "properties": null,
  "sortOrder": 0,
  "status": "VALID",
  "note": ""
}
```

``` http request
### PATCH SysProperty
PATCH http://localhost:8080/_rest/sysProperty/359040721228922880
Accept: application/json
Content-Type: application/json

{
  "status": "INVALID"
}
```

``` http request
### DELETE SysProperties
DELETE http://localhost:8080/_rest/sysProperty?ids=359040721228922880,359040721228922990
Accept: application/json
```

``` http request
### DELETE SysProperty
DELETE http://localhost:8080/_rest/sysProperty/364097947324907520
Accept: application/json
```


### 自定义查询

对于自定义查询，也提供了简单的支持。

例如在Mapper中定义查询：

``` java
@Select("SELECT t.* FROM sys_properties t WHERE category = #{category} and status = 'VALID' ORDER BY sort_order")
IPage<SysProperty> selectByCategory(IPage page, String category);
```

相对应的查询方法会自动发布，通过 
``` http request
GET /_rest/sysProperty/search/selectByCategory?category=FILE_TYPE&page=1&size=2&sort=category,asc&sort=sortOrder,asc
```
调用
