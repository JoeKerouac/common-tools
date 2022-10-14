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
package com.github.joekerouac.common.tools.registry;

import java.io.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.resource.impl.ClassPathResource;

/**
 * 注册表核心API
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public final class RegistryKey implements Closeable {

    /**
     * 对应注册表的 HKEY_CLASSES_ROOT
     */
    public static final RegistryKey HKEY_CLASSES_ROOT;

    /**
     * 对应注册表的 HKEY_CURRENT_USER
     */
    public static final RegistryKey HKEY_CURRENT_USER;

    /**
     * 对应注册表的 HKEY_LOCAL_MACHINE
     */
    public static final RegistryKey HKEY_LOCAL_MACHINE;

    /**
     * 对应注册表的 HKEY_USERS
     */
    public static final RegistryKey HKEY_USERS;

    /**
     * 对应注册表的 HKEY_PERFORMANCE_DATA
     */
    public static final RegistryKey HKEY_PERFORMANCE_DATA;

    /**
     * 对应注册表的 HKEY_CURRENT_CONFIG
     */
    public static final RegistryKey HKEY_CURRENT_CONFIG;

    /**
     * 对应注册表的 HKEY_DYN_DATA
     */
    public static final RegistryKey HKEY_DYN_DATA;

    /*
     * 权限相关详细说明参考文档：https://docs.microsoft.com/en-us/windows/win32/sysinfo/registry-key-security-and-access-rights
     */

    /**
     * 对应KEY_READ
     */
    public static final int ACCESS_DEFAULT = 0;

    /**
     * 对应KEY_READ
     */
    public static final int ACCESS_READ = 1;

    /**
     * 对应KEY_WRITE
     */
    public static final int ACCESS_WRITE = 2;

    /**
     * 相当于{@link #ACCESS_READ}，对应KEY_EXECUTE
     */
    public static final int ACCESS_EXECUTE = 3;

    /**
     * 对应KEY_ALL_ACCESS
     */
    public static final int ACCESS_ALL = 4;

    /**
     * jni中会反射调用该字段获取值 <br/>
     * <br/>
     * 这是从注册表api返回的实际dword键，对于注册表外部来说应该是完全不透明的，这个值不应该被引用
     */
    private final int key;

    /**
     * 注册表全路径
     */
    private final String name;

    /**
     * 当调用createSubKey时会使用，返回的key的该字段如果是true表示创建成功，返回false表示已经存在了
     */
    protected final boolean created;

    private RegistryKey(int key, String name) {
        this.key = key;
        this.name = name;
        this.created = false;
    }

    /**
     * jni中c代码调用
     * 
     * @param key
     *            key
     * @param name
     *            name
     * @param created
     *            是否是新建的注册表项（在创建的时候如果返回的该值是false表示要创建的注册表已经存在了）
     */
    private RegistryKey(int key, String name, boolean created) {
        this.key = key;
        this.name = name;
        this.created = created;
    }

    @Override
    public void close() throws IOException {
        if (this.name.indexOf("\\") > 0) {
            this.closeKey();
        }
    }

    /**
     * 获取该注册表项的相对名称
     * 
     * @return 相对名称
     */
    public String getName() {
        int index = this.name.lastIndexOf("\\");

        if (index < 0) {
            return this.name;
        } else {
            return this.name.substring(index + 1);
        }
    }

    /**
     * 获取该注册表的全称
     * 
     * @return 全称
     */
    public String getFullName() {
        return this.name;
    }

    /**
     * 该注册表项是否是新建的
     * 
     * @return 如果该注册表项是被新建出来的则返回true
     */
    public boolean wasCreated() {
        return this.created;
    }

    /**
     * 打开注册表子key
     *
     * @param subKey
     *            子key名
     * @return 注册表子key
     * @throws NoSuchKeyException
     *             要打开的key不存在
     * @throws RegistryException
     *             其他异常
     */
    public RegistryKey openSubKey(String subKey) throws NoSuchKeyException, RegistryException {
        return this.openSubKey(subKey, ACCESS_READ);
    }

    /**
     * 打开一个子注册表项
     * 
     * @param subKey
     *            子注册表名
     * @param access
     *            指定打开时使用的访问权限
     * @return 子注册表
     * @throws NoSuchKeyException
     *             指定的注册表名不存在
     * @throws RegistryException
     *             其他异常
     */
    public native RegistryKey openSubKey(String subKey, int access) throws NoSuchKeyException, RegistryException;

    /**
     * 连接远程的注册表
     * 
     * @param hostName
     *            hostName
     * @return 注册表
     * @throws NoSuchKeyException
     *             注册表不存在
     * @throws RegistryException
     *             其他异常
     */
    public native RegistryKey connectRegistry(String hostName) throws NoSuchKeyException, RegistryException;

    /**
     * 创建注册表子项
     *
     * @param subKey
     *            子项名
     * @return 创建的子注册表
     * @throws RegistryException
     *             异常
     */
    public RegistryKey createSubKey(String subKey) throws RegistryException {
        return this.createSubKey(subKey, "", ACCESS_WRITE);
    }

    /**
     * 创建注册表子项
     *
     * @param subKey
     *            子项名
     * @param access
     *            访问权限
     * @return 创建的子注册表
     * @throws RegistryException
     *             异常
     */
    public RegistryKey createSubKey(String subKey, int access) throws RegistryException {
        return this.createSubKey(subKey, "", access);
    }

    /**
     * 创建本注册表的子项
     * 
     * @param subKey
     *            子项名
     * @param className
     *            这个key的用户定义类型，可能会被忽略，可以是null
     * @param access
     *            要创建的注册表项的访问权限
     * @return 创建的注册表子项
     * @throws RegistryException
     *             异常
     */
    private native RegistryKey createSubKey(String subKey, String className, int access) throws RegistryException;

    /**
     * 关闭本注册表
     * 
     * @throws RegistryException
     *             注册表异常
     */
    public native void closeKey() throws RegistryException;

    /**
     * 删除子注册表项
     * 
     * @param subKey
     *            子注册表名
     * @throws NoSuchKeyException
     *             指定的注册表名不存在
     * @throws RegistryException
     *             其他异常
     */
    public native void deleteSubKey(String subKey) throws NoSuchKeyException, RegistryException;

    /**
     * 删除注册表value
     * 
     * @param valueName
     *            valueName
     * @throws NoSuchValueException
     *             指定的注册表value不存在
     * @throws RegistryException
     *             其他异常
     */
    public native void deleteValue(String valueName) throws NoSuchValueException, RegistryException;

    /**
     * 保证将本注册表写入磁盘，调用开销较大，仅应在需要的时候调用
     * 
     * @throws RegistryException
     *             异常
     */
    public native void flushKey() throws RegistryException;

    /**
     * 往注册表中写入一个value
     *
     * @param value
     *            要写入的value
     * @throws RegistryException
     *             异常
     */
    public void setValue(RegistryValue value) throws RegistryException {
        this.setValue(value.getName(), value);
    }

    /**
     * 为注册表设置value
     * 
     * @param valueName
     *            valueName
     * @param value
     *            value
     * @throws RegistryException
     *             异常
     */
    private native void setValue(String valueName, RegistryValue value) throws RegistryException;

    /**
     * 获取注册表的指定value
     * 
     * @param valueName
     *            valueName
     * @return value值
     * @throws NoSuchValueException
     *             指定value不存在
     * @throws RegistryException
     *             其他异常
     */
    public native RegistryValue getValue(String valueName) throws NoSuchValueException, RegistryException;

    /**
     * 获取String类型的value
     * 
     * @param valueName
     *            valueName
     * @return value值
     * @throws NoSuchValueException
     *             指定value不存在
     * @throws RegistryException
     *             其他异常
     */
    public native String getStringValue(String valueName) throws NoSuchValueException, RegistryException;

    /**
     * 获取默认value
     * 
     * @return 默认value值
     * @throws NoSuchValueException
     *             没有默认value
     * @throws RegistryException
     *             其他异常
     */
    public native String getDefaultValue() throws NoSuchValueException, RegistryException;

    /**
     * 判断注册表是否有默认value
     * 
     * @return true表示有默认value
     * @throws RegistryException
     *             异常
     */
    public native boolean hasDefaultValue() throws RegistryException;

    /**
     * 判断注册表是否只有默认value
     * 
     * @return true表示只有默认value
     * @throws RegistryException
     *             异常
     */
    public native boolean hasOnlyDefaultValue() throws RegistryException;

    /**
     * 获取该注册表包含的子注册表的数量
     * 
     * @return 子注册表数量
     * @throws RegistryException
     *             异常
     */
    public native int getNumberSubkeys() throws RegistryException;

    /**
     * 获取所有子项名称的长度最大的那个长度值
     * 
     * @return 所有子项名称的长度最大的那个长度值
     * @throws RegistryException
     *             异常
     */
    public native int getMaxSubKeyLength() throws RegistryException;

    /**
     * 获取注册表的指定下标的子项名称
     * 
     * @param index
     *            子项下标
     * @return 子项名称
     * @throws RegistryException
     *             异常
     */
    public native String regEnumKey(int index) throws RegistryException;

    /**
     * 获取此注册表包含的value数量
     * 
     * @return 包含的value的数量
     * @throws RegistryException
     *             异常
     */
    public native int getNumberValues() throws RegistryException;

    /**
     * 获取所有value数据中数据最长的数据的长度
     * 
     * @return 所有value数据中数据最长的数据的长度
     * @throws RegistryException
     *             异常
     */
    public native int getMaxValueDataLength() throws RegistryException;

    /**
     * 获取valueName最长的那个长度值
     * 
     * @return valueName最长的那个长度值
     * @throws RegistryException
     *             异常
     */
    public native int getMaxValueNameLength() throws RegistryException;

    /**
     * 获取注册表指定下标的value的名称
     * 
     * @param index
     *            value下标
     * @return value名称
     * @throws RegistryException
     *             异常
     */
    public native String regEnumValue(int index) throws RegistryException;

    /**
     * 将指定 REG_DWORD 类型的value的值增加1
     * 
     * @param valueName
     *            valueName
     * @return 增加后的值
     * @throws NoSuchValueException
     *             指定value不存在
     * @throws RegistryException
     *             其他异常
     */
    public native int incrDoubleWord(String valueName) throws NoSuchValueException, RegistryException;

    /**
     * 将指定 REG_DWORD 类型的value的值减少1
     * 
     * @param valueName
     *            valueName
     * @return 减小后的值
     * @throws NoSuchValueException
     *             指定value不存在
     * @throws RegistryException
     *             其他异常
     */
    public native int decrDoubleWord(String valueName) throws NoSuchValueException, RegistryException;

    /**
     * 替换字符串中的环境变量引用，例如 %PATH% 将会替换为环境变量中实际的PATH值
     * 
     * @param exString
     *            指定字符串模板
     * @return 将字符串模板中的环境变量引用替换后的字符串
     */
    public static native String expandEnvStrings(String exString);

    /**
     * 获取该注册表下边的所有子注册表的名称
     * 
     * @return 该注册表下边的所有子注册表的名称
     * @throws RegistryException
     *             异常
     */
    public Enumeration<String> keyElements() throws RegistryException {
        return new RegistryKeyEnumerator(this);
    }

    /**
     * 获取该注册表下边所有value的名称
     * 
     * @return 该注册表下所有value的名称
     * @throws RegistryException
     *             异常
     */
    public Enumeration<String> valueElements() throws RegistryException {
        return new RegistryValueEnumerator(this);
    }

    static {
        if (Const.IS_WINDOWS) {
            // 随机dll name
            String dllName = UUID.randomUUID().toString();
            // 临时目录
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File dllFile = new File(tmpDir, dllName + ".dll");

            ClassPathResource x64Dll = new ClassPathResource("jni/registry/release/x64/registry.dll");
            ClassPathResource x86Dll = new ClassPathResource("jni/registry/release/x86/registry.dll");

            // 优先加载64位的dll，如果64位的无法加载则加载32位的dll，另外注意，这个只能再Windows平台使用
            try {
                loadLibrary(dllFile, x64Dll);
            } catch (UnsatisfiedLinkError error) {
                loadLibrary(dllFile, x86Dll);
            }

            HKEY_CLASSES_ROOT = new RegistryKey(0x80000000, "HKEY_CLASSES_ROOT");
            HKEY_CURRENT_USER = new RegistryKey(0x80000001, "HKEY_CURRENT_USER");
            HKEY_LOCAL_MACHINE = new RegistryKey(0x80000002, "HKEY_LOCAL_MACHINE");
            HKEY_USERS = new RegistryKey(0x80000003, "HKEY_USERS");
            HKEY_PERFORMANCE_DATA = new RegistryKey(0x80000004, "HKEY_PERFORMANCE_DATA");
            HKEY_CURRENT_CONFIG = new RegistryKey(0x80000005, "HKEY_CURRENT_CONFIG");
            HKEY_DYN_DATA = new RegistryKey(0x80000006, "HKEY_DYN_DATA");
        } else {
            System.err.println("当前系统不是Windows，无法使用注册表相关功能");
            HKEY_CLASSES_ROOT = null;
            HKEY_CURRENT_USER = null;
            HKEY_LOCAL_MACHINE = null;
            HKEY_USERS = null;
            HKEY_PERFORMANCE_DATA = null;
            HKEY_CURRENT_CONFIG = null;
            HKEY_DYN_DATA = null;
        }
    }

    /**
     * 加载dll，将dll复制到临时文件并加载
     * 
     * @param tmpFile
     *            dll临时文件
     * @param dllResource
     *            dll文件
     */
    private static void loadLibrary(File tmpFile, Resource dllResource) {
        try {
            OutputStream outputStream = new FileOutputStream(tmpFile);
            IOUtils.write(outputStream, dllResource.getInputStream(), true);
            outputStream.close();
            System.load(tmpFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class RegistryKeyEnumerator implements Enumeration<String> {
        RegistryKey key;

        int currIndex;

        int numSubKeys;

        public RegistryKeyEnumerator(RegistryKey key) throws RegistryException {
            this.key = key;
            this.currIndex = 0;
            this.numSubKeys = key.getNumberSubkeys();
        }

        public boolean hasMoreElements() {
            return (this.currIndex < this.numSubKeys);
        }

        public String nextElement() {
            String result = null;

            try {
                result = this.key.regEnumKey(this.currIndex++);
            } catch (RegistryException ex) {
                throw new NoSuchElementException(ex.getMessage());
            }

            return result;
        }
    }

    static class RegistryValueEnumerator implements Enumeration<String> {

        RegistryKey key;

        int currIndex;

        int numValues;

        public RegistryValueEnumerator(RegistryKey key) throws RegistryException {
            this.key = key;
            this.currIndex = 0;
            this.numValues = key.getNumberValues();
        }

        public boolean hasMoreElements() {
            return (this.currIndex < this.numValues);
        }

        public String nextElement() {
            String result;

            try {
                result = this.key.regEnumValue(this.currIndex++);
            } catch (RegistryException ex) {
                throw new NoSuchElementException(ex.getMessage());
            }

            return result;
        }
    }

}
