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
package com.github.joekerouac.common.tools.reflect.type;

import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.reflect.ClassUtils;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * java类型工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaTypeUtil {

    /**
     * super泛型，匹配? super Object这种泛型
     */
    private static final Pattern SUPER_PATTERN = Pattern.compile("(.*) super.*");

    /**
     * extends泛型，匹配? extends Object这种泛型
     */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("(.*) extends.*");

    /**
     * 将指定类型转换为Class，例如如果类型是带泛型的Map，则将泛型信息擦除，返回Map的class对象；
     * 
     * @param type
     *            类型
     * @return class，有可能返回null
     */
    public static Class<?> toClass(Type type) {
        final JavaType javaType = createJavaType(type);
        final JavaType realType = getRealType(javaType);
        if (realType instanceof BaseType) {
            return ((BaseType)realType).getType();
        } else if (realType instanceof CustomGenericArrayType) {
            JavaType componentType = ((CustomGenericArrayType)realType).getComponentType();
            final Class<?> componentClass = toClass(componentType);
            if (componentClass != null) {
                int[] dimensions = new int[((CustomGenericArrayType)realType).getDimensions()];
                return Array.newInstance(componentClass, dimensions).getClass();
            }
        }

        return null;
    }

    /**
     * 创建一个没有泛型的基本类型
     * 
     * @param clazz
     *            基本类型Class
     * @return 对应的JavaType
     */
    public static BaseType createBaseType(Class<?> clazz) {
        BaseType baseType = new BaseType();
        baseType.setType(clazz);
        baseType.setName(clazz.getSimpleName());
        return baseType;
    }

    /**
     * 根据TypeReference得出自定义类型
     *
     * @param type
     *            TypeReference
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(AbstractTypeReference<?> type) {
        return createJavaType(type.getType());
    }

    /**
     * 根据java系统类型得出自定义类型
     *
     * @param type
     *            java反射取得的类型
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(Type type) {
        return createJavaType(type, new HashMap<>());
    }

    /**
     * 根据java系统类型得出自定义类型
     *
     * @param type
     *            java反射取得的类型
     * @param resolved
     *            当前已经确定的泛型，该参数主要解决递归依赖问题，例如泛型声明&lt;T extends List&lt;T&gt;&gt;，如果没有该参数，解析
     *            到List的泛型列表的时候将会重新解析T，这样就会死循环，为了解决这个问题，所以引入了该字段
     * @return 自定义java类型说明
     */
    private static JavaType createJavaType(Type type, Map<GenericDefinition, JavaType> resolved) {
        // type的来源：1、从方法参数上获取；2、从字段上获取；3、从类上获取；4、从继承上获取
        if (type instanceof JavaType) {
            return (JavaType)type;
        }

        String typeName = dealName(type.getTypeName());
        JavaType javaType;
        if (type instanceof WildcardType) {
            // 该类型是不确定的泛型，即泛型为 ?，说明该泛型没有任何声明，直接使用了，类似与匿名内部类；
            WildcardType wildcardTypeImpl = (WildcardType)type;
            // 子类
            Type[] child = wildcardTypeImpl.getLowerBounds();
            // 父类
            Type[] parent = wildcardTypeImpl.getUpperBounds();
            javaType = new GenericType();
            GenericType genericType = (GenericType)javaType;

            // child和parent不可能都为空，如果用户是使用的一个单泛型T或者?，没有明确指出他的父类或者子类，例如T extends String、
            // T super String，那么就会有一个默认的parent，值是Object
            if (child.length > 0) {
                genericType.setChild(createJavaType(child[0], resolved));
            } else {
                genericType.setParent(createJavaType(parent[0], resolved));
            }

        } else if (type instanceof TypeVariable) {
            // 该类型是名字确定的泛型，例如T等，需要先声明后使用，区别于WildcardType，WildcardType类型的泛型不需要声明可以直接使用；
            TypeVariable<?> typeVariableImpl = (TypeVariable<?>)type;

            // 获取该泛型声明的地方，带名字的泛型目前声明的地方只有三种可能：1、类；2、方法；3、构造器；
            GenericDeclaration genericDeclaration = typeVariableImpl.getGenericDeclaration();

            javaType = new GenericType();
            GenericType genericType = (GenericType)javaType;
            // 设置该泛型的声明处，声明+名字唯一确定一个泛型类型
            genericType.setGenericDeclaration(genericDeclaration);

            // 需要先将该泛型放入以确定的泛型，防止后边setParent的时候出现死循环，对于这种泛型声明将会出现死循环：
            // T extends List<T>，解析List的时候由于List还有泛型，并且这个是T，如果不做处理，将会循环解析T，最终陷入
            // 死循环，所以这里在泛型解析完成前（解析parent前）先放入map防止死循环
            resolved.put(new GenericDefinition(typeName, genericDeclaration), genericType);

            // 指定名字的泛型只能继承，不能使用关键字super，所以getBounds该方法得出的是泛型的父类型，getBounds肯定有一个值，如果
            // 用户没有指定就是Object
            genericType.setParent(createJavaType(typeVariableImpl.getBounds()[0], resolved));
        } else if (type instanceof ParameterizedType) {
            // 该类型存在泛型
            ParameterizedType parameterizedTypeImpl = (ParameterizedType)type;
            Type[] types = parameterizedTypeImpl.getActualTypeArguments();
            JavaType[] generics = new JavaType[types.length];

            for (int i = 0; i < types.length; i++) {
                Type nowType = types[i];

                // 如果是命名泛型，优先从注册表中找
                if (nowType instanceof TypeVariable) {
                    TypeVariable<?> typeVariable = (TypeVariable<?>)nowType;
                    GenericDefinition genericDefinition =
                        new GenericDefinition(typeVariable.getName(), typeVariable.getGenericDeclaration());
                    generics[i] = resolved.get(genericDefinition);
                }

                // 注册表中找不到，继续创建一个新的
                if (generics[i] == null) {
                    generics[i] = createJavaType(types[i], resolved);
                }
            }

            javaType = new BaseType();
            BaseType baseType = (BaseType)javaType;
            baseType.setType((Class<?>)parameterizedTypeImpl.getRawType());
            baseType.setGenerics(generics);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType)type;
            javaType = getArrayDesc(genericArrayType, resolved);
        } else if (type instanceof Class) {
            // 该类型是普通类型（没有泛型，本身也不是泛型参数）
            Class<?> clazz = (Class<?>)type;

            if (clazz.isArray()) {
                javaType = getArrayDesc(clazz, resolved);
            } else {
                BaseType baseType = new BaseType();
                baseType.setType(clazz);
                javaType = baseType;
            }
        } else {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, "type[" + type + "]类型未知");
        }

        javaType.setName(typeName);

        return javaType;
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @return 数组说明，如果指定类型不是数组，那么抛出异常
     */
    public static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type) {
        return getArrayDesc(type, new HashMap<>());
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @param resolved
     *            当前已经确定的类型，允许为空
     * @return 数组说明，如果指定类型不是数组，那么抛出异常
     */
    public static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type,
        Map<GenericDefinition, JavaType> resolved) {
        Assert.assertTrue(((type instanceof Class) && ((Class<?>)type).isArray()) || (type instanceof GenericArrayType),
            "类型必须是数组类型", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        return getArrayDesc(type, 0, resolved);
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @param now
     *            当前的数组维度
     * @param resolved
     *            当前已经确定的类型，允许为空
     * @return 数组说明
     */
    private static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type, int now,
        Map<GenericDefinition, JavaType> resolved) {
        if (type instanceof Class) {
            if (((Class<?>)type).isArray()) {
                return getArrayDesc(((Class<?>)type).getComponentType(), now + 1, resolved);
            }
        } else if (type instanceof GenericArrayType) {
            return getArrayDesc(((GenericArrayType)type).getGenericComponentType(), now + 1, resolved);
        }

        CustomGenericArrayType arrayDesc = new CustomGenericArrayType();
        arrayDesc.setComponentType(createJavaType(type, resolved));
        arrayDesc.setDimensions(now);
        return arrayDesc;
    }

    /**
     * 获取指定类继承父类时使用的泛型列表，例如有如下类：
     * <p>
     * <code>public class Test extends U&lt;String&gt;</code>
     * <p>
     * 对该类使用该方法将会获得{@link String}对应的JavaType
     *
     * @param clazz
     *            类型
     * @return 类继承父类时使用的泛型列表
     */
    public static List<JavaType> getDeclareGenericSuperclasses(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");

        Type genericSuperclass = clazz.getGenericSuperclass();

        // 如果不是ParameterizedType，说明类继承的时候没有使用泛型
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return Collections.emptyList();
        }

        ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return Arrays.stream(actualTypeArguments).map(JavaTypeUtil::createJavaType).collect(Collectors.toList());
    }

    /**
     * 获取指定类上声明的泛型列表，例如有如下类
     * <p>
     * <code>public class Test&lt;T,F&gt;</code>
     * <p>
     * 对该类使用该方法将会返回T和M这两个泛型
     * 
     * @param clazz
     *            指定Class
     * @return 类上声明的泛型列表
     */
    public static List<JavaType> getDeclareGenerics(Class<?> clazz) {
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
        if (CollectionUtil.isEmpty(typeParameters)) {
            return Collections.emptyList();
        }

        return Arrays.stream(typeParameters).map(JavaTypeUtil::createJavaType).collect(Collectors.toList());
    }

    /**
     * 获取方法上的注册表
     * 
     * @param method
     *            指定方法
     * @return 方法上的泛型注册表
     */
    public static Map<GenericDefinition, JavaType> getGenericRegistry(Method method) {
        Map<GenericDefinition, JavaType> map = new HashMap<>();
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        for (TypeVariable<Method> typeParameter : typeParameters) {
            JavaType javaType = JavaTypeUtil.createJavaType(typeParameter);
            GenericDefinition genericDefinition =
                new GenericDefinition(javaType.getName(), typeParameter.getGenericDeclaration());
            map.put(genericDefinition, getRealType(javaType));
        }

        return map;
    }

    /**
     * 获取指定childClass中涉及的所有泛型定义并返回，并且递归获取父类中的泛型定义，一直到指定的父类parentClass停止，并且尝试将父类的泛型转换 为子类中定义的泛型或者继承的泛型，例如对于以下类：
     * <p>
     * <code><br/>
     *     class A&lt;T, F&gt;<br/>
     *     class B&lt;T, F&gt; extends A&lt;String, T&gt;{}<br/>
     * </code><br/>
     * </p>
     * 传入childClass B、parentClass A将会解析出来四个泛型，一个是B中定义的T，实际类型为Object，一个是B中定义的F，实际类型是Object，还有
     * 一个是A中定义的T，实际类型是String，还有一个是A中定义的类型F，实际类型是B中定义的泛型T，也就是A中的泛型定义T此时是确定的，是String的
     * 子类（实际就是String，因为String是final无法继承），而A中另外一个泛型定义F则对应B中的泛型定义T，只有B中泛型定义T确定了A中的泛型定义
     * F才会确认，而如果我们想要进一步缩小泛型B中定义的两个泛型的范围，只有外部传入一些已知条件，在outerResolved中指定B中的两个泛型定义分别对 应什么类型，这样我们才能进一步缩小泛型B中定义的两个泛型的范围；
     * 
     * @param childClass
     *            childClass
     * @param parentClass
     *            parentClass
     * @param outerResolved
     *            外围已知的类型注册表
     * @return 父类中声明的所有泛型对应的实际类型，如果父类没有声明则返回空
     */
    public static Map<GenericDefinition, JavaType> getGenericRegistry(Class<?> childClass, Class<?> parentClass,
        Map<GenericDefinition, JavaType> outerResolved) {
        // 构建子类到父类的链
        List<Class<?>> list = new ArrayList<>();
        list = ClassUtils.buildChain(childClass, parentClass, list);

        Map<GenericDefinition, JavaType> resolved =
            new HashMap<>(outerResolved == null ? Collections.emptyMap() : outerResolved);

        // 存放前一次解析出来的泛型类型，例如A extends B<String>，解析A的时候可以解析出来B的泛型实际上是String，后边解析B的时候可以将
        // 这个作为已知类型注册进来；为什么不在上一轮注册到已知类型中？因为上一轮（解析A的时候）我们只知道B有一个泛型，并且类型是String，但是
        // 我们并不知道这个泛型的名字
        List<JavaType> frontResolved = Collections.emptyList();
        // 注意，这里的顺序不能边，一定是要从子类开始解析
        for (int i = 0; i < list.size(); i++) {
            Class<?> clazz = list.get(i);

            // 获取本class上定义的泛型，例如class A<T, F>定义，将会返回T、F的泛型
            TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();

            // 存储当前所有待解析的类型
            Map<GenericDefinition, JavaType> waitResolve = new HashMap<>();

            for (int j = 0; j < typeParameters.length; j++) {
                TypeVariable<?> typeVariable = typeParameters[j];
                JavaType javaType = createJavaType(typeVariable);

                GenericDefinition genericDefinition =
                    new GenericDefinition(javaType.getName(), typeVariable.getGenericDeclaration());

                // 如果上一次解析出来的类型不为空，则将其作为已知类型注册到已知类型注册表中
                // 因为上一次解析的时候并不能知道泛型的名字，泛型名字到本次才知道，所以在这里注册
                if (!frontResolved.isEmpty()) {
                    resolved.put(genericDefinition, frontResolved.get(j));
                }

                waitResolve.put(genericDefinition, javaType);
            }

            // 解析目前等待解析的类型
            typeResolve(waitResolve, resolved);

            // 继承的时候为父类型声明的泛型，例如A extends B<String>，那么这里就有一个String的类型
            JavaType[] generics = null;
            // 这里获取父类的类型，肯定是BaseType，然后有可能包含一些泛型
            if (clazz.getGenericSuperclass() != null && !clazz.getGenericSuperclass().equals(Object.class)) {
                JavaType parent = createJavaType(clazz.getGenericSuperclass());
                Assert.assertTrue(parent instanceof BaseType, "预期外的场景",
                    ExceptionProviderConst.IllegalStateExceptionProvider);
                generics = ((BaseType)parent).getGenerics();
            }

            if (generics != null) {
                // 这里直接解析
                frontResolved =
                    Arrays.stream(generics).map(generic -> typeResolve(generic, resolved)).collect(Collectors.toList());
            } else {
                // 注意，这里要清空状态，否则有可能被前边的环境污染；
                frontResolved = Collections.emptyList();
            }
        }

        return resolved;
    }

    /**
     * 如果入参是泛型，那么获取泛型的真正类型，如果泛型无法正确解析，则使用Object代替（如果是T super XX这种场景，也使用Object代替），如果
     * 是BaseType或者CustomGenericArrayType，不会对其包含的泛型处理，这里会直接返回
     * 
     * @param type
     *            Java类型说明
     * @return 真正的类型
     */
    public static JavaType getRealType(JavaType type) {
        return getRealType(type, Collections.emptyMap());
    }

    /**
     * 如果入参是泛型，那么获取泛型的真正类型，如果泛型无法正确解析，则使用Object代替（如果是T super XX这种场景，也使用Object代替），如果
     * 是BaseType或者CustomGenericArrayType，不会对其包含的泛型处理，这里会直接返回
     * 
     * @param type
     *            Java类型说明
     * @param genericRegistry
     *            当前上下文注册过的泛型和其对应的类型，传入这个主要为了辅助类型推断用的，例如如果方法上声明了泛 型T extends
     *            User，然后在参数中直接使用了类型T，如果我们只解析参数是没办法解析出来T实际上是什么类型，所 以我们要当前上下文注册的所有已知泛型对应的类型
     * @return 真正的类型，当泛型是循环的场景，返回的还是泛型，例如 T extends List&lt;T&gt;
     */
    public static JavaType getRealType(JavaType type, Map<GenericDefinition, JavaType> genericRegistry) {
        Assert.argNotNull(type, "type");
        Assert.argNotNull(genericRegistry, "genericRegistry");

        String typeName = type.getName();
        if (type instanceof GenericType) {
            // 注意，泛型如果是 T super User这种形式，这里获取到的将不准确，因为T实际上是父类，并不能将其转为对应的子类，但是这里这么做了
            GenericType genericType = (GenericType)type;

            // 优先从上下文中注册的泛型类型查找，这里genericType.getGenericDeclaration()可能为空，不过不影响
            GenericDefinition genericDefinition = new GenericDefinition(typeName, genericType.getGenericDeclaration());
            JavaType javaType = genericRegistry.get(genericDefinition);
            if (javaType != null) {
                // 注册表中已经找到类型了，直接返回
                return javaType;
            } else {
                // 上下文注册的泛型类型中查找不到，开始推测
                JavaType parent = genericType.getParent();
                // 如果parent等于null，说明child不等于null，说明当前泛型是T super XX的格式，目前不支持，直接到最后抛出异常即可
                if (parent != null) {
                    return getRealType(parent, genericRegistry);
                }
            }
        } else if (type instanceof BaseType || type instanceof CustomGenericArrayType) {
            return type;
        }
        // 如果类型未知或者是泛型，但是泛型是T super XX形式的，抛出异常
        throw new CommonException(ErrorCodeEnum.CODE_ERROR,
            StringUtils.format("不支持的类型或者场景（当前泛型是T super XX的场景）：[{}]", type.getClass()));
    }

    /**
     * 类型解析，如果传入类型是泛型，返回其对应的实际类型，如果传入类型是普通类型，将其包含的所有泛型也解析为对应的实际类型，如果泛型无法确认， 则使用Object代替；该方法处理完毕理论上JavaType中应该不包含泛型类型了
     * 
     * @param type
     *            要解析的类型
     * @param genericRegistry
     *            当前上下文注册过的泛型和其对应的类型，传入这个主要为了辅助类型推断用的，例如如果方法上声明了泛 型T extends
     *            User，然后在参数中直接使用了类型T，如果我们只解析参数是没办法解析出来T实际上是什么类型，所 以我们需要当前上下文注册的所有已知泛型对应的类型
     * @return 处理后的类型
     */
    public static JavaType typeResolve(JavaType type, Map<GenericDefinition, JavaType> genericRegistry) {
        if (type instanceof GenericType) {
            // 如果类型是泛型，则确定该泛型的实际类型，而该泛型的实际类型有可能还包含了泛型，所以需要递归去resolve，同时genericRegistry在
            // 解析该泛型包含的泛型时仍然是有效的；同时，如果泛型已经在注册表中有解释的话，就不再resolve了
            GenericDefinition genericDefinition =
                new GenericDefinition(type.getName(), ((GenericType)type).getGenericDeclaration());
            JavaType resolved = genericRegistry.get(genericDefinition);
            if (resolved == null) {
                // 将本次解析到的类型注册，防止出现递归死循环，例如 T extends List<T>场景
                resolved = typeResolve(getRealType(type, genericRegistry), genericRegistry);
                genericRegistry.put(genericDefinition, resolved);
            }
            return resolved;
        } else if (type instanceof BaseType) {
            // 如果类型是基本类型，那么解析该类型的所有泛型为实际类型
            BaseType baseType = (BaseType)type;
            JavaType[] generics = baseType.getGenerics();

            // 获取本类型上定义的泛型，此处generics中使用的泛型定义都肯定是在类型本身上边定义的
            TypeVariable<? extends Class<?>>[] typeParameters = baseType.getType().getTypeParameters();

            if (CollectionUtil.sizeEquals(typeParameters, generics)) {
                // 如果typeParameters和generics长度一致，有可能都是空，也有可能都不是空，所以这里要先判断先是否不是空，不是空的时候才说明
                // 有泛型声明
                if (CollectionUtil.size(generics) > 0) {
                    JavaType[] newGenerics = new JavaType[generics.length];

                    // 处理涉及到的泛型，将其都解析为对应的BaseType
                    for (int i = 0; i < generics.length; i++) {
                        // 如果使用的时候指定了泛型，那么指定的泛型范围必须比声明的泛型范围小，所以这里直接用当前指定的泛型解析肯定就是范围
                        // 较小的
                        // 使用当前注册表解析出一个类型
                        JavaType resolved = typeResolve(generics[i], genericRegistry);

                        newGenerics[i] = resolved;
                    }

                    baseType.setGenerics(newGenerics);
                }
            } else {
                /*
                 * 如果这两个不相等，肯定是generics为空，而typeParameters不为空，对应类上声明的有泛型，但是实际使用的时候没有明确写入泛型；
                 * 这时应该使用类上的泛型，因为类上声明的时候还有可能限定了泛型类型，例如类上如果使用T extends XX这种形式声明，那我们就可以将
                 * 泛型范围确定在XXX了
                 */

                // 这里做下兜底校验，只要generics是空，那么typeParameters肯定不是空，因为他们两个size不相等；这样就符合我们上边的前提了；
                Assert.isEmpty(generics, "预期外的场景", ExceptionProviderConst.IllegalStateExceptionProvider);

                JavaType[] newGenerics = new JavaType[typeParameters.length];
                for (int i = 0; i < typeParameters.length; i++) {
                    // 注意，这里解析的时候不能使用genericRegistry作为注册表传入了，这个在父类范围已经不适用了
                    JavaType resolved = typeResolve(
                        getRealType(createJavaType(typeParameters[i]), Collections.emptyMap()), Collections.emptyMap());
                    newGenerics[i] = resolved;
                }
                baseType.setGenerics(newGenerics);
            }

            return baseType;
        } else if (type instanceof CustomGenericArrayType) {
            // 泛型数组，例如T[]
            CustomGenericArrayType customGenericArrayType = (CustomGenericArrayType)type;
            JavaType componentType = customGenericArrayType.getComponentType();
            customGenericArrayType.setComponentType(typeResolve(componentType, genericRegistry));
            return customGenericArrayType;
        } else {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION,
                StringUtils.format("不支持的类型：[{}]", type.getClass()));
        }
    }

    /**
     * 根据已知泛型定义解析所有未知泛型定义
     *
     * @param waitResolve
     *            等待解析的泛型声明和其对应的类型
     * @param resolved
     *            当前已知的泛型声明对应的实际类型
     * @return 将所有waitResolve中的泛型全部解析放入resolved并返回
     */
    private static Map<GenericDefinition, JavaType> typeResolve(Map<GenericDefinition, JavaType> waitResolve,
        Map<GenericDefinition, JavaType> resolved) {
        List<GenericDefinition> waitResolveKey = new ArrayList<>(waitResolve.keySet());

        for (GenericDefinition genericDefinition : waitResolveKey) {
            JavaType javaType = waitResolve.remove(genericDefinition);

            // 有可能已经提前解析过了，不处理，继续下一个
            if (javaType == null) {
                continue;
            }

            // 解析该类型是否依赖其他泛型，如果依赖其他泛型，那么肯定在本类中定义，需要先解析其他可能依赖的泛型
            List<GenericDefinition> genericDefinitions = resolveRequireGenericName(javaType);
            // 是否还需要先解析其他的
            boolean needResolveOther = false;
            for (GenericDefinition definition : genericDefinitions) {
                if (!resolved.containsKey(definition)) {
                    needResolveOther = true;
                    break;
                }
            }

            if (needResolveOther) {
                // 想尝试解析其他所有未知类型，里边就有可能包含我们依赖的类型，如果没有的话后边将会使用默认类型
                typeResolve(waitResolve, resolved);
            }

            // 好了，到这里我们的依赖就肯定都解析完成并且放入resolved了，下面就可以开始解析javaType了
            javaType = typeResolve(javaType, resolved);
            resolved.put(genericDefinition, javaType);
        }

        return resolved;
    }

    /**
     * 将原始类型的class转换为对应的包装类型
     * 
     * @param primitiveClazz
     *            原始类型
     * @return 对应的包装类型，如果传入的不是原始类型将会抛出异常
     */
    public static Class<?> boxed(Class<?> primitiveClazz) {
        Class<?> result;

        if (boolean.class.equals(primitiveClazz)) {
            result = Boolean.class;
        } else if (char.class.equals(primitiveClazz)) {
            result = Character.class;
        } else if (byte.class.equals(primitiveClazz)) {
            result = Byte.class;
        } else if (short.class.equals(primitiveClazz)) {
            result = Short.class;
        } else if (int.class.equals(primitiveClazz)) {
            result = Integer.class;
        } else if (long.class.equals(primitiveClazz)) {
            result = Long.class;
        } else if (double.class.equals(primitiveClazz)) {
            result = Double.class;
        } else if (float.class.equals(primitiveClazz)) {
            result = Float.class;
        } else {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, String.format("类型[%s]不是原始类型", primitiveClazz));
        }

        return result;
    }

    /**
     * 判断Class对象是否为指定的几种简单类型（该方法认为java自带简单类型包括java八大基本类型及其对应的封装类型、Number的子类、String、
     * Collection的子类、Map的子类、Enum、Date、Temporal（新版日期API），如果不是这些类型将会认为该类型是一个复杂类型（pojo类型））
     *
     * @param clazz
     *            Class对象，不能为null
     * @return 如果是pojo则返回<code>false</code>
     * @throws NullPointerException
     *             当传入Class对象为null时抛出该异常
     */
    public static boolean isNotPojo(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return isSimple(clazz) || Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
            || Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否是八大基本类型、枚举类型、String、Number类型
     * 
     * @param clazz
     *            Class
     * @return 如果不是以上几种类型返回false
     */
    public static boolean isSimple(Class<?> clazz) {
        return isGeneralType(clazz) || isBasic(clazz) || Enum.class.isAssignableFrom(clazz)
            || String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class对象是否为八大基本类型的封装类型
     *
     * @param clazz
     *            Class对象，不能为null
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为null时抛出该异常
     */
    public static boolean isBasic(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");

        return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)
            || Byte.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)
            || Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
            || Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz);
    }

    /**
     * 判断指定Class是否是8大基本类型（int、short等，不包含对应的封装类型）
     *
     * @param clazz
     *            class对象
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralType(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return clazz.isPrimitive();
    }

    /**
     * 判断指定数组名称是否是java八大基本类型（int[]、short[]、int[][]等，不包含对应的封装类型）
     *
     * @param clazz
     *            指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralArrayType(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return clazz.isArray() && isGeneralType(toClass(getArrayDesc(clazz).getComponentType()));
    }

    /**
     * 将JVM类型签名转换为JVM的class对象
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @return Class对象
     */
    public static Class<?> signToClass(String jvmClassName) {
        return signToClass(jvmClassName, ClassUtils.getDefaultClassLoader());
    }

    /**
     * 将JVM类型签名转换为JVM的class对象
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @param classLoader
     *            ClassLoader
     * @return Class对象
     */
    public static Class<?> signToClass(String jvmClassName, ClassLoader classLoader) {
        return ClassUtils.loadClass(signToClassName(jvmClassName), classLoader);
    }

    /**
     * 将JVM类型签名转换为JVM的ClassName，例如Ljava/lang/String;
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @return ClassName，原生类型比较特殊，是int、boolean、byte等，数组的ClassName和签名一致，所以将会原封不动返回
     */
    public static String signToClassName(String jvmClassName) {
        Assert.argNotBlank(jvmClassName, "jvmClassName");
        String packageClass = jvmClassName.trim().replaceAll("/", ".");

        String baseClass = null;
        if ("Z".equals(packageClass)) {
            baseClass = boolean.class.getName();
        } else if ("C".equals(packageClass)) {
            baseClass = char.class.getName();
        } else if ("B".equals(packageClass)) {
            baseClass = byte.class.getName();
        } else if ("S".equals(packageClass)) {
            baseClass = short.class.getName();
        } else if ("I".equals(packageClass)) {
            baseClass = int.class.getName();
        } else if ("J".equals(packageClass)) {
            baseClass = long.class.getName();
        } else if ("D".equals(packageClass)) {
            baseClass = double.class.getName();
        } else if ("F".equals(packageClass)) {
            baseClass = float.class.getName();
        } else if (packageClass.startsWith("L") && packageClass.endsWith(";")) {
            baseClass = packageClass.substring(1, packageClass.length() - 1);
        } else if (packageClass.startsWith("[")) {
            // 数组，不用管
            baseClass = packageClass;
        }

        if (baseClass == null) {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION,
                StringUtils.format("指定类型[{}]无法解析", packageClass));
        }

        return baseClass;
    }

    /**
     * 解析指定类型中依赖的所有泛型（不包含?）
     * 
     * @param javaType
     *            指定类型
     * @return 依赖的泛型
     */
    private static List<GenericDefinition> resolveRequireGenericName(JavaType javaType) {
        List<GenericDefinition> genericDefinitions = new ArrayList<>();
        if (javaType instanceof BaseType) {
            BaseType baseType = (BaseType)javaType;
            JavaType[] generics = baseType.getGenerics();
            if (!CollectionUtil.isEmpty(generics)) {
                for (JavaType generic : generics) {
                    genericDefinitions.addAll(resolveRequireGenericName(generic));
                }
            }
        } else if (javaType instanceof GenericType) {
            GenericType genericType = (GenericType)javaType;
            if (genericType.getGenericDeclaration() != null) {
                genericDefinitions
                    .add(new GenericDefinition(genericType.getName(), genericType.getGenericDeclaration()));
            }

            if (genericType.getParent() != null) {
                genericDefinitions.addAll(resolveRequireGenericName(genericType.getParent()));
            } else {
                genericDefinitions.addAll(resolveRequireGenericName(genericType.getChild()));
            }
        }
        return genericDefinitions;
    }

    /**
     * 处理T extends XX或者T super XX这种类型的泛型名称
     * 
     * @param fullName
     *            泛型全名
     * @return 泛型的名称
     */
    private static String dealName(String fullName) {
        Matcher matcher = SUPER_PATTERN.matcher(fullName);
        String name;
        if (matcher.find()) {
            name = matcher.group(1);
        } else {
            matcher = EXTENDS_PATTERN.matcher(fullName);
            if (matcher.find()) {
                name = matcher.group(1);
            } else {
                name = fullName;
            }
        }
        return name;
    }

}
