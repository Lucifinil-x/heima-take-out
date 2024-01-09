package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 所有封装分页查询结果都会封装成PageResult对象，再封装到result才能返回给前端。
 * 接口文档中需要返回给前端的属性，第一层是code,msg,data(对应result中的属性),
 * 第二层是data包含total,records(对应PageResult中属性)
 */
@Data
@AllArgsConstructor //可以调用有参数的构造方法
@NoArgsConstructor
public class PageResult implements Serializable {

    private long total; //总记录数

    private List records; //当前页数据集合

}
