package com.github.joekerouac.common.tools.io;

/**
 * @author JoeKerouac
 * @date 2023-06-09 10:54
 * @since 2.0.3
 */
public interface DataFilter {

    /**
     * 过滤数据
     * 
     * @param ref
     *            过滤前的数据
     * @return 过滤后的数据
     */
    ByteBufferRef filter(ByteBufferRef ref);

}
