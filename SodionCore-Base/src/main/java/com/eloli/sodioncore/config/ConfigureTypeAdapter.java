package com.eloli.sodioncore.config;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;

public class ConfigureTypeAdapter extends TypeAdapter<Configure> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (Configure.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new ConfigureTypeAdapter(gson);
            }
            return null;
        }
    };

    private final Gson gson;

    public ConfigureTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(JsonWriter out, Configure value) throws IOException {
        out.beginObject();
        for (Field field : value.getClass().getFields()) {
            String translatedName = gson.fieldNamingStrategy().translateName(field);
            Lores lores = field.getAnnotation(Lores.class);
            if (lores != null) {
                for (int i = 0; i < lores.value().length; i++) {
                    out.name("_" + translatedName + "_" + i);
                    out.value(lores.value()[i].value());
                }
            } else {
                Lore lore = field.getAnnotation(Lore.class);
                if (lore != null) {
                    out.name("_" + translatedName);
                    out.value(lore.value());
                }
            }
            out.name(translatedName);
            TypeAdapter<Object> typeAdapter = gson.getAdapter((Class<Object>) field.getType());
            try {
                typeAdapter.write(out, field.get(value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        out.endObject();
    }

    @Override
    public Configure read(JsonReader in) throws IOException {
        throw new IOException("Try read with writer.");
    }
}
