package com.fighter.common.utils;

import android.os.Build;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * 判空相关工具类
 */
public class EmptyUtils {
    private EmptyUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 判断是否包含空对象
     *
     * @param objs 待判断的对象
     * @return true 包含空对象 <br></br> false 全部不为空
     */
    public static boolean isEmpty(Object... objs) {
        if (objs == null) {
            return true;
        }
        for (Object obj : objs) {
            if (isEmpty(obj)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否所有对象不为空
     *
     * @param objs 待判断对象
     * @return true 所有对象不为空 <br></br> false 包含空对象
     */
    public static boolean isNotEmpty(Object... objs) {
        if (objs == null) {
            return false;
        }
        for (Object obj : objs) {
            if (isEmpty(obj)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 对象
     * @return {@code true}: 为空<br>{@code false}: 不为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String && obj.toString().length() == 0) {
            return true;
        }
        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return true;
        }
        if (obj instanceof Collection && ((Collection) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof Map && ((Map) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof SparseArray && ((SparseArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseBooleanArray && ((SparseBooleanArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseIntArray && ((SparseIntArray) obj).size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (obj instanceof SparseLongArray && ((SparseLongArray) obj).size() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断对象是否非空
     *
     * @param obj 对象
     * @return {@code true}: 非空<br>{@code false}: 空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
