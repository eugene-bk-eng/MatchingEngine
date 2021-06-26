package com.ea.matchingengine.util;

import com.ea.matchingengine.engine.MatchingEngine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public class UtilReflection {

    public static <T extends MatchingEngine> T loadInstance(Class<T> type, String loadClass, Class constructorClz, Object constructorArg ) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> cl = Class.forName(loadClass);
        Constructor<?> cons = cl.getConstructor(constructorClz);
        Object instance = cons.newInstance(constructorArg);
        return type.cast(instance);
    }
}
