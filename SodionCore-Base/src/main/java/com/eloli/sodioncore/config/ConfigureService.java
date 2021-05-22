package com.eloli.sodioncore.config;

import com.eloli.sodioncore.file.BaseFileService;
import com.google.gson.JsonObject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ConfigureService<C extends Configure> {
    protected final File configFile;
    protected final String configName;
    protected final BaseFileService fileService;
    protected final HashMap<Integer, Class<? extends Configure>> configureMap = new HashMap<>();
    protected final HashMap<Integer, Class<? extends Migrater<?, ?>>> migraterMap = new HashMap<>();
    public C instance;
    protected int currentVersion;

    public ConfigureService(BaseFileService fileService, String configName) {
        configFile = new File(fileService.getConfigPath(configName));
        this.configName = configName;
        this.fileService = fileService;
    }

    public ConfigureService<C> register(Class<? extends Migrater<?, ?>> migraterClass, Class<? extends Configure> configureClass) {
        migraterMap.put(migraterMap.size(), migraterClass);
        configureMap.put(configureMap.size(), configureClass);
        return this;
    }

    @SuppressWarnings("unchecked")
    public ConfigureService<C> init() throws Exception {
        this.currentVersion = configureMap.size() - 1;
        fileService.saveResource("probeResource", true);
        try {
            FileInputStream fileReader = new FileInputStream(configFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileReader, StandardCharsets.UTF_8);
            JsonObject result = ConfigGson.reader.fromJson(inputStreamReader, JsonObject.class);
            inputStreamReader.close();
            fileReader.close();
            int version = result.get("version").getAsInt();
            if (version < currentVersion) {
                instance = (C) migrate(version, ConfigGson.reader.fromJson(result, getConfigure(version)));
            } else {
                instance = (C) ConfigGson.reader.fromJson(result, getConfigure(currentVersion));
            }
        } catch (FileNotFoundException e) {
            instance = (C) getConfigure(currentVersion).getConstructor().newInstance();
        }

        save();
        return this;
    }

    private Configure migrate(int fromVersion, Configure configure) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (fromVersion == currentVersion) {
            return configure;
        }
        Configure result = getConfigure(fromVersion + 1).getConstructor().newInstance();
        replace(result, configure);
        if (getMigrator(fromVersion + 1) != null) {
            getMigrator(fromVersion + 1).getConstructor().newInstance().migrate(configure, result);
        }
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
    protected Class<? extends Migrater<Configure, Configure>> getMigrator(int toVersion) {
        return (Class<? extends Migrater<Configure, Configure>>) migraterMap.get(toVersion);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Configure> getConfigure(int version) throws ClassNotFoundException {
        return configureMap.get(version);
    }

    public void save() throws IOException {
        FileOutputStream fileWriter = new FileOutputStream(configFile);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileWriter, StandardCharsets.UTF_8);
        outputStreamWriter.write(ConfigGson.writer.toJson(instance));
        outputStreamWriter.flush();
        outputStreamWriter.close();
        fileWriter.flush();
        fileWriter.close();
    }
}
