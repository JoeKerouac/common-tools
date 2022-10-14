/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.joekerouac.common.tools.reference;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * 自定义equals方法的WeakReference，如果包含的引用不为null则比较包含的引用；
 * 
 * 注意：引用指向的对象的hashCode运行时不能发生改变，否则会出现问题
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class CustomWeakReference<T> extends WeakReference<T> {

    /**
     * hashCode
     */
    private final int hashCode;

    public CustomWeakReference(T referent) {
        super(referent);
        // 直接初始化hashCode，不能每次调用，因为后边引用可能会被回收
        this.hashCode = referent.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CustomWeakReference)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        CustomWeakReference<T> reference = (CustomWeakReference<T>)obj;

        // 比较引用
        return this.hashCode == reference.hashCode && Objects.equals(reference.get(), this.get());
    }
}
