package com.fighter.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class RefInvoker {
    private static final String TAG = "RefInvoker";

    private static final ClassLoader CLASS_LOADER_SYSTEM = ClassLoader.getSystemClassLoader();
    private static final ClassLoader CLASS_LOADER_BOOT   = CLASS_LOADER_SYSTEM.getParent();
    private static final ClassLoader CLASS_LOADER_APP    = RefInvoker.class.getClassLoader();

    private static HashMap<String, Class> sClassCache = new HashMap<>();

    public static Class forName(String clazzName) throws ClassNotFoundException {
        Class clazz = sClassCache.get(clazzName);
        if (clazz == null) {
            clazz = Class.forName(clazzName);
            ClassLoader cl = clazz.getClassLoader();
            if (cl == CLASS_LOADER_SYSTEM || cl == CLASS_LOADER_APP || cl == CLASS_LOADER_BOOT) {
                sClassCache.put(clazzName, clazz);
            }
        }
        return clazz;
    }

    public static Object newInstance(String className, Class[] paramTypes, Object[] paramValues) {
        try {
            Class clazz = forName(className);
            Constructor constructor = clazz.getConstructor(paramTypes);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(paramValues);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object target, String className, String methodName,
                                      Class[] paramTypes, Object[] paramValues) {
        Class clazz = null;
        try {
            clazz = forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz == null) return null;
        return invokeMethod(target, clazz, methodName, paramTypes, paramValues);
    }

    public static Object invokeMethod(Object target, Class clazz, String methodName,
                                      Class[] paramTypes, Object[] paramValues) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (method == null) {
            try {
                method = clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        if (method != null) {
            method.setAccessible(true);
            try {
                return method.invoke(target, paramValues);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getField(Object target, String className, String fieldName) {
        try {
            Class clazz = forName(className);
            return getField(target, clazz, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getField(Object target, Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // try supper for Miui, Miui has a class named MiuiPhoneWindow
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception superE) {
                e.printStackTrace();
                superE.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    @SuppressWarnings("rawtypes")
    public static void setField(Object target, String className,
                                String fieldName, Object fieldValue) {
        try {
            Class clazz = forName(className);
            setField(target, clazz, fieldName, fieldValue);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setField(Object target, Class clazz, String fieldName, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, fieldValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // try supper for Miui, Miui has a class named MiuiPhoneWindow
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(target, fieldValue);
            } catch (Exception superE) {
                e.printStackTrace();
                //superE.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Method findMethod(Object object, String methodName, Class[] clazzes) {
        try {
            return object.getClass().getDeclaredMethod(methodName, clazzes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}