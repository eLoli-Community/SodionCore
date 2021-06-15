package com.eloli.sodioncore.dependency.classloader;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

public class ReflectionClassLoader {
    private final URLClassLoader classLoader;
    private final Method addUrlMethod;
    private final Method clearAssertionStatusMethod;
    private final Field invalidClassesField;

    public ReflectionClassLoader() {
        ClassLoader classLoader = ReflectionClassLoader.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
            addUrlMethod = getFirstMethodImpl(classLoader.getClass(),"addURL", URL.class);
            addUrlMethod.setAccessible(true);

            clearAssertionStatusMethod = getFirstMethodImpl(classLoader.getClass(),"clearAssertionStatus");

            Field invalidClassesField;
            try {
                invalidClassesField = classLoader.getClass().getDeclaredField("invalidClasses");
            } catch (NoSuchFieldException e) {
                invalidClassesField = null;
            }
            this.invalidClassesField = invalidClassesField;
        } else {
            throw new IllegalStateException("ClassLoader is not a instance of URLClassLoader");
        }
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public boolean testClasses(String testClass){
        if(invalidClassesField != null){
            return false;
        }
        try {
            Class.forName(testClass);
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    public void cleanClassCache(String testClass){
        if(invalidClassesField != null){
            try {
                ((Set<String>) invalidClassesField.get(this.classLoader))
                        .remove(testClass);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addJarToClasspath(Path file) {
        try {
            addUrlMethod.invoke(this.classLoader,
                    file.toUri().toURL());
            clearAssertionStatusMethod.invoke(this.classLoader);
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Method getFirstMethodImpl(Class<?> clazz, String name, Class<?>... parameterTypes){
        try {
            return clazz.getDeclaredMethod(name,parameterTypes);
        } catch (NoSuchMethodException e) {
            return getFirstMethodImpl(clazz.getSuperclass(),name,parameterTypes);
        }
    }
}
