package com.eloli.sodioncore.channel;

import com.eloli.sodioncore.channel.util.FieldWrapper;
import com.eloli.sodioncore.channel.util.Priority;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

public abstract class PluginMessagePacket {
    public static List<FieldWrapper> resolveFieldWrapperList(Class<? extends PluginMessagePacket> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<FieldWrapper> fieldWrapperList = Lists.newArrayList();
        for (Field field : fields) {
            Priority codecProprety = field.getAnnotation(Priority.class);
            if (codecProprety == null) {
                continue;
            }
            FieldWrapper fw = new FieldWrapper(field, codecProprety);
            fieldWrapperList.add(fw);
        }

        fieldWrapperList.sort(Comparator.comparingInt(o -> o.getPriority().value()));

        return fieldWrapperList;
    }

    public abstract List<FieldWrapper> getFieldWrapperList();
}
