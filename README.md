# Data Rest

参照Spring Data Rest，一个基于Mybatis Plus的自定义实现，支持简单的CRUD REST接口

Mybatis Plus中，Mapper是继承BaseMapper，如：interface SysPropertyMapper extends BaseMapper<SysProperty>

REST接口的调用：GET /_rest/sysProperty，匹配对应的SysPropertyMapper，调用SysPropertyMapper的selectList()


