package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author qiyu
 * @create 2019-07-24 12:23
 * @Description:TODO(这里用一句话来描述这个类的作用)
 */
@Data
public class BaseCatalog3 implements Serializable{

    @Id
    @Column
    private String id;
    @Column
    private String name;
    @Column
    private String catalog2Id;
}
