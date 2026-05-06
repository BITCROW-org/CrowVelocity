package com.velocitypowered.proxy.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonConfig {

    private final File file;
    private final Gson gson;
    private JsonObject data;

    public JsonConfig(File file, Gson gson) {
        this.file = file;
        this.gson = gson;
    }

    public void load() {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            if (content.isEmpty()) {
                data = new JsonObject();
                return;
            }

            data = gson.fromJson(content, JsonObject.class);

            if (data == null) {
                data = new JsonObject();
            }

        } catch (Exception e) {
            data = new JsonObject();
        }
    }

    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            data = new JsonObject();
        }
    }

    public String getString(String path, String def) {
        JsonElement el = get(path);

        if (el != null && el.isJsonPrimitive()) {
            return el.getAsString();
        }

        return def;
    }

    public int getInt(String path, int def) {
        JsonElement el = get(path);

        if (el != null && el.isJsonPrimitive()) {
            return el.getAsInt();
        }

        return def;
    }

    public boolean getBoolean(String path, boolean def) {
        JsonElement el = get(path);

        if (el != null && el.isJsonPrimitive()) {
            return el.getAsBoolean();
        }

        return def;
    }

    public JsonElement getOrDefault(String path, Object def) {
        JsonElement el = get(path);

        if (el == null) {
            set(path, def);
            save();
            return gson.toJsonTree(def);
        }

        return el;
    }

    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        JsonObject current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i]) || !current.get(parts[i]).isJsonObject()) {
                JsonObject obj = new JsonObject();
                current.add(parts[i], obj);
                current = obj;
            } else {
                current = current.getAsJsonObject(parts[i]);
            }
        }

        current.add(parts[parts.length - 1], gson.toJsonTree(value));
    }

    public JsonElement get(String path) {
        String[] parts = path.split("\\.");
        JsonObject current = data;

        for (int i = 0; i < parts.length; i++) {
            if (!current.has(parts[i])) {
                return null;
            }

            if (i == parts.length - 1) {
                return current.get(parts[i]);
            }

            if (!current.get(parts[i]).isJsonObject()) {
                return null;
            }

            current = current.getAsJsonObject(parts[i]);
        }

        return null;
    }
}