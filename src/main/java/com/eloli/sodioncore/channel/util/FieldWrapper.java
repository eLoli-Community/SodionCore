package com.eloli.sodioncore.channel.util;

import java.lang.reflect.Field;

public class FieldWrapper {
    private final Field field;
    private final Priority priority;

    public FieldWrapper(Field field, Priority priority) {
        this.field = field;
        this.priority = priority;
    }

    public Field getField() {
        return field;
    }

    public Priority getPriority() {
        return priority;
    }
}