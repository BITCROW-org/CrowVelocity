/*
 * Copyright (C) 2026 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Object plugin;
    private final File baseDir;
    private final Gson gson;
    private final Map<String, JsonConfig> configs = new HashMap<>();

    public ConfigManager(Object plugin, File baseDir) {
        this.plugin = plugin;
        this.baseDir = baseDir;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        if (!baseDir.exists()) baseDir.mkdirs();
    }

    public JsonConfig load(String name) {
        File file = new File(baseDir, name + ".json");

        if (!file.exists()) copyDefault(name + ".json", file);

        JsonConfig config = new JsonConfig(file, gson);
        config.load();
        configs.put(name, config);
        return config;
    }

    private void copyDefault(String resource, File target) {
        try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                target.getParentFile().mkdirs();
                target.createNewFile();
                return;
            }

            target.getParentFile().mkdirs();
            Files.copy(in, target.toPath());
        } catch (Exception ignored) {
        }
    }

    public JsonConfig get(String name) {
        return configs.get(name);
    }
}