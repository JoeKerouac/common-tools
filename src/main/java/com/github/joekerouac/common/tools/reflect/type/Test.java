package com.github.joekerouac.common.tools.reflect.type;

import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.util.JsonUtil;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JoeKerouac
 * @date 2023-06-20 18:12
 * @since
 */
public class Test {


    /**
     * super泛型，匹配? super Object这种泛型
     */
    private static final Pattern SUPER_PATTERN = Pattern.compile("(.*) super.*");

    /**
     * extends泛型，匹配? extends Object这种泛型
     */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("(.*) extends.*");


    public static void main(String[] args) throws Exception {
        Map<GenericDefinition, JavaType> context = new HashMap<>();

        Field data1 = GenericData.class.getDeclaredField("data1");

        Type type = new AbstractTypeReference<GenericDataB>() {
        }.getType();
        JavaType javaType = JavaTypeUtil.createJavaType(type);
        System.out.println(javaType);

    }




    private static void resolve(Field field, Map<String, Class<?>> resolved) {
        Type type = field.getGenericType();



        Class<?> resolvedType;

        String typeName = dealName(type.getTypeName());

        if (type instanceof WildcardType) {
            // 该类型是不确定的泛型，即泛型为 ?，说明该泛型没有任何声明，直接使用了，类似与匿名内部类；
            WildcardType wildcardTypeImpl = (WildcardType)type;

            // 子类
            Type[] child = wildcardTypeImpl.getLowerBounds();
            // 父类
            Type[] parent = wildcardTypeImpl.getUpperBounds();

            // child和parent不可能都为空，如果用户是使用的一个单泛型T或者?，没有明确指出他的父类或者子类，例如T extends String、
            // T super String，那么就会有一个默认的parent，值是Object
            if (child.length > 0) {
                // FIXME 确认类型
//                child[0];
            } else {
                // FIXME 确认类型
//                parent[0];
            }

        } else if (type instanceof TypeVariable) {
            // 该类型是名字确定的泛型，例如T等，需要先声明后使用，区别于WildcardType，WildcardType类型的泛型不需要声明可以直接使用；
            TypeVariable<?> typeVariableImpl = (TypeVariable<?>)type;
            // 泛型名
            String name = typeVariableImpl.getName();


            // 获取该泛型声明的地方，带名字的泛型目前声明的地方只有三种可能：1、类；2、方法；3、构造器；因为我们这里是字段，所以肯定来源于类
            GenericDeclaration genericDeclaration = typeVariableImpl.getGenericDeclaration();
            TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();

        } else if (type instanceof ParameterizedType) {

        } else if (type instanceof GenericArrayType) {

        } else if (type instanceof Class<?>) {
            resolvedType = (Class<?>) type;
        }
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

    private static class FieldInfo {

        /**
         * 真实类型
         */
        private Class<?> resolvedType;

    }


    public static class Prop {

    }

    public static class A {

    }
    public static class B extends A{

    }

    public static class C extends B {

    }

    static abstract class GenericData<T, M> {
        private T data1;
        private M data2;
        private List<T> listData;
    }

    static class GenericDataA<M extends A, T extends A> extends GenericData<T, M> {
        private M m;
    }


    static class GenericDataB<M extends A, T extends B> extends GenericDataA<T, M> {
        private M m;
    }




}
