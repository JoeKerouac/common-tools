package com.github.joekerouac.common.tools.reflect.type;

import java.util.LinkedHashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author JoeKerouac
 * @date 2023-06-21 11:45
 * @since 2.0.3
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SimpleType extends JavaType {

    /**
     * 该类型声明的泛型
     */
    private LinkedHashMap<String, JavaType> bindings;

    /**
     * {@link #bindings}.values()
     */
    private List<JavaType> bindingList;

}
