package fudge.spatialcrafting.common.util;

import fudge.spatialcrafting.SpatialCrafting;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private ReflectionUtil() {}

    public static <T> void callPrivateMethod(Class<T> clazz, T object, String methodName, Class[] argTypes, Object[] args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, argTypes);
            method.invoke(object, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            SpatialCrafting.LOGGER.error(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <OBJT, FIELDT> FIELDT getPrivateField(Class<OBJT> clazz, @Nullable OBJT object, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (FIELDT) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            SpatialCrafting.LOGGER.error(e);
            return null;
        }
    }

}
