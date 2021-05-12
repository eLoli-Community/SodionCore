package com.eloli.sodioncore.dependency.classloader;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ReflectionClassLoader {
    private final URLClassLoader classLoader;
    private final Method addUrlMethod;

    public ReflectionClassLoader() {
        ClassLoader classLoader = ReflectionClassLoader.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
            try {
                addUrlMethod = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException("ClassLoader is not a instance of URLClassLoader");
        }
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void addJarToClasspath(Path file) {
        try {
            addUrlMethod.invoke(this.getClass().getClassLoader(),
                    file.toUri().toURL());
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
