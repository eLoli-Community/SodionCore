package com.eloli.sodioncore.channel.util;

import com.eloli.sodioncore.channel.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PayloadEncoder {

    public static <T extends PluginMessagePacket> byte[] getPayload(int id, T command) {
        List<FieldWrapper> fieldWrapperList = command.getFieldWrapperList();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(id);
        fieldWrapperList.forEach(fieldWrapper -> write2ByteBuf(fieldWrapper, command, buffer));
        byte[] result = new byte[buffer.writerIndex()];
        buffer.readBytes(result);
        return result;
    }

    private static void write2ByteBuf(FieldWrapper fieldWrapper, Object instance, ByteBuf buffer) {
        Field field = fieldWrapper.getField();
        field.setAccessible(true);
        Object value;
        try {
            value = field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (field.getType().isArray()) {
            int length = Array.getLength(value);
            buffer.writeInt(length);
            for (int i = 0; i < length; i++) {
                writeValue(fieldWrapper, buffer, field.getType().getComponentType(), Array.get(value, i));
            }
        } else {
            writeValue(fieldWrapper, buffer, field.getType(), value);
        }
    }

    private static void writeValue(FieldWrapper fieldWrapper, ByteBuf buffer, Class<?> type, Object value) {
        Field field = fieldWrapper.getField();
        String typeName = type.getName();
        switch (typeName) {
            case "java.lang.Boolean":
            case "boolean":
                buffer.writeBoolean((Boolean) value);
                break;
            case "java.lang.Character":
            case "char":
                buffer.writeChar((Character) value);
                break;
            case "java.lang.Byte":
            case "byte":
                buffer.writeByte((byte) value);
                break;
            case "java.lang.Short":
            case "short":
                buffer.writeShort((short) value);
                break;
            case "java.lang.Integer":
            case "int":
                buffer.writeInt((int) value);
                break;
            case "java.lang.Long":
            case "long":
                buffer.writeLong((long) value);
                break;
            case "java.lang.Float":
            case "float":
                buffer.writeFloat((float) value);
                break;
            case "java.lang.Double":
            case "double":
                buffer.writeDouble((double) value);
                break;
            case "java.lang.String":
                byte[] subValue = ((String) value).getBytes(StandardCharsets.UTF_8);
                buffer.writeInt(subValue.length);
                buffer.writeBytes(subValue);
                break;
            default:
                if (PluginMessagePacket.class.isAssignableFrom(field.getType())) {
                    List<FieldWrapper> subFieldWrapperList = ((PluginMessagePacket) value).getFieldWrapperList();
                    for (FieldWrapper subFieldWrapper : subFieldWrapperList) {
                        write2ByteBuf(subFieldWrapper, value, buffer);
                    }
                } else {
                    throw new RuntimeException("SodionCore.channel still not support " + typeName + ".");
                }
        }
    }
}
