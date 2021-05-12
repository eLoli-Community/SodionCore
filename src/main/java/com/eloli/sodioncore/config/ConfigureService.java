package com.eloli.sodioncore.config;

import com.eloli.sodioncore.file.BaseFileService;
import com.google.gson.JsonObject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class ConfigureService<C extends Configure> {
    public static final int CURRENT_VERSION = 2;

    protected final String classPrefix;
    protected final File configFile;
    protected final BaseFileService fileService;

    public C instance;

    @SuppressWarnings("unchecked")
    public ConfigureService(BaseFileService fileService, String configName, String classPrefix) throws Exception {
        this.fileService = fileService;
        this.classPrefix = classPrefix;
        configFile = new File(fileService.getConfigPath(configName));
        fileService.saveResource("probeResource", true);
        try {
            FileInputStream fileReader = new FileInputStream(configFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileReader, StandardCharsets.UTF_8);
            JsonObject result = ConfigGson.reader.fromJson(inputStreamReader, JsonObject.class);
            inputStreamReader.close();
            fileReader.close();
            int version = result.get("version").getAsInt();
            if (version < CURRENT_VERSION) {
                instance = (C) migrate(version, ConfigGson.reader.fromJson(result, getConfigure(version)));
            } else {
                instance = (C) ConfigGson.reader.fromJson(result, Class.forName(classPrefix + ".MainConfiguration"));
            }
        } catch (FileNotFoundException e) {
            instance = (C) Class.forName(classPrefix + ".MainConfiguration").getConstructor().newInstance();
        }

        save();

    }

    private Configure migrate(int fromVersion, Configure configure) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (fromVersion == CURRENT_VERSION) {
            return configure;
        }
        Configure result = getConfigure(fromVersion + 1).getConstructor().newInstance();
        replace(result, configure);
        getMigrator(fromVersion).getConstructor().newInstance().migrate(configure, result);
        return migrate(fromVersion + 1, result);
    }

    private Configure replace(Configure configure, Configure getter) throws IllegalAccessException, NoSuchFieldException {
        for (Field field : configure.getClass().getFields()) {
            Class<?> type = field.getType();
            if (Configure.class.isAssignableFrom(type)) {
                field.set(configure, replace((Configure) field.get(configure), getter));
            } else {
                Migrate annotation = field.getAnnotation(Migrate.class);
                if (annotation != null) {
                    field.set(configure, getHistoryValue(getter, annotation.value()));
                }
            }
        }
        return configure;
    }

    private Object getHistoryValue(Configure getter, String path) throws NoSuchFieldException, IllegalAccessException {
        String[] paths = path.split("\\.");
        if (paths.length == 1) {
            return getter.getClass().getField(paths[0]).get(getter);
        } else {
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 1; i < paths.length; i++) {
                pathBuilder.append(".").append(paths[i]);
            }
            return getHistoryValue(
                    (Configure) getter.getClass().getField(paths[0]).get(getter),
                    pathBuilder.substring(1)
            );
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Migrater<Configure, Configure>> getMigrator(int version) throws ClassNotFoundException {
        return (Class<? extends Migrater<Configure, Configure>>)
                Class.forName(classPrefix + ".migrates.MigrateFrom" + version);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Configure> getConfigure(int version) throws ClassNotFoundException {
        if (version == CURRENT_VERSION) {
            return (Class<? extends Configure>) Class.forName(classPrefix + ".MainConfiguration");
        } else {
            return (Class<? extends Configure>)
                    Class.forName(classPrefix + ".migrates.HistoryConfiguration" + version);
        }
    }

    protected void save() throws IOException {
        FileOutputStream fileWriter = new FileOutputStream(configFile);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileWriter, StandardCharsets.UTF_8);
        outputStreamWriter.write(ConfigGson.writer.toJson(instance));
        outputStreamWriter.flush();
        outputStreamWriter.close();
        fileWriter.flush();
        fileWriter.close();
    }
}
