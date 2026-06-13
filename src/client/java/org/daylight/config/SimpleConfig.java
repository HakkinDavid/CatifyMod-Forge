package org.daylight.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleConfig {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Class.class, new TypeAdapter() {
                @Override
                public void write(JsonWriter out, Object value) throws IOException {
                    out.value(((Class<?>) value).getName());
                }

                @Override
                public Class<?> read(JsonReader in) throws IOException {
                    try {
                        return Class.forName(in.nextString());
                    } catch (ClassNotFoundException e) {
                        throw new IOException(e);
                    }
                }
            })
            .create();
    private static final File FILE = new File("config/catify.json");
    private Map<String, Object> data = new HashMap<>();

    public void load() {
        if (!FILE.exists()) return;
        try (Reader reader = new FileReader(FILE)) {
            data = GSON.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }
}