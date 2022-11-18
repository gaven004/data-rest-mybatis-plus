package com.g.data.rest;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_properties")
public class SysProperty implements java.io.Serializable {
    private Long id;
    private String category;
    private String name;
    private String value;
    private String properties;
    private Integer sortOrder;
    private String status;
    private String note;
}
