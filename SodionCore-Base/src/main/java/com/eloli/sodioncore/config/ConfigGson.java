package com.eloli.sodioncore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigGson {
    public static Gson reader;
    public static Gson writer;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setFieldNamingStrategy(field -> {
            String name = field.getName();
            Name nameAnnotation = field.getAnnotation(Name.class);
            if (nameAnnotation != null) {
                if (nameAnnotation.plain()) {
                    return nameAnnotation.value();
                } else {
                    name = nameAnnotation.value();
                }
            }
            StringBuilder translation = new StringBuilder();
            int i = 0;
            for (int length = name.length(); i < length; ++i) {
                char character = name.charAt(i);
                if (Character.isUpperCase(character) && translation.length() != 0) {
                    translation.append(" ");
                }
                translation.append(character);
            }
            return translation.toString().toLowerCase();
        });
        builder.serializeNulls();
        builder.setPrettyPrinting();
        builder.disableHtmlEscaping();
        reader = builder.create();
        builder.registerTypeAdapterFactory(ConfigureTypeAdapter.FACTORY);
        writer = builder.create();
    }
}
