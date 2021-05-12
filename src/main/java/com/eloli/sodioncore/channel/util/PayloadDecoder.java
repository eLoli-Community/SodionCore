package com.eloli.sodioncore.channel.util;

import com.eloli.sodioncore.channel.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PayloadDecoder {
    public static <T extends PluginMessagePacket> T resolve(byte[] src, Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        List<FieldWrapper> fieldWrapperList = instance.getFieldWrapperList();
        ByteBuf buffer = Unpooled.buffer().writeBytes(src);
        buffer.readInt();
        for (FieldWrapper fieldWrapper : fieldWrapperList) {
            fillData(fieldWrapper, instance, buffer);
        }

        return instance;
    }

    private static void fillData(FieldWrapper fieldWrapper, Object instance, ByteBuf buffer) {
        Field field = fieldWrapper.getField();
        field.setAccessible(true);
        try {
            if (field.getType().isArray()) {
                int length = buffer.readInt();
                Object arrayValue = Array.newInstance(field.getType().getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Array.set(arrayValue, i, getData(fieldWrapper, buffer, field.getType().getComponentType()));
                }
                field.set(instance, arrayValue);
            } else {
                field.set(instance, getData(fieldWrapper, buffer, field.getType()));
            }
        } catch (Exception e) {
            throw new RuntimeException("读取失败，field:" + field.getName(), e);
        }
    }

    private static Object getData(FieldWrapper fieldWrapper, ByteBuf buffer, Class<?> type) {
        Field field = fieldWrapper.getField();
        String typeName = type.getName();
        switch (typeName) {
            case "java.lang.Boolean":
            case "boolean":
                return buffer.readBoolean();
            case "java.lang.Character":
            case "char":
                return buffer.readChar();
            case "java.lang.Byte":
            case "byte":
                return buffer.readByte();
            case "java.lang.Short":
            case "short":
                return buffer.readShort();
            case "java.lang.Integer":
            case "int":
                return buffer.readInt();
            case "java.lang.Long":
            case "long":
                return buffer.readLong();
            case "java.lang.Float":
            case "float":
                return buffer.readFloat();
            case "java.lang.Double":
            case "double":
                return buffer.readDouble();
            case "java.lang.String":
                int length = buffer.readInt();
                byte[] subValue = new byte[length];
                buffer.readBytes(subValue);
                return new String(subValue, StandardCharsets.UTF_8);
            default:
                if (PluginMessagePacket.class.isAssignableFrom(field.getType())) {
                    PluginMessagePacket subInstance;
                    try {
                        subInstance = (PluginMessagePacket) field.getType().getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    List<FieldWrapper> subFieldWrapperList = subInstance.getFieldWrapperList();
                    for (FieldWrapper subFieldWrapper : subFieldWrapperList) {
                        fillData(subFieldWrapper, subInstance, buffer);
                    }
                    return subInstance;
                } else {
                    throw new RuntimeException("SodionCore.channel still not support " + typeName + ".");
                }
        }
    }
}
