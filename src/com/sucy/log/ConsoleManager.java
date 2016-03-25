/**
 * LogManager
 * com.sucy.log.LogManager
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.log;

import com.sucy.log.config.CommentedConfig;
import com.sucy.log.config.DataSection;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Simple utility for controlled what plugins and the server log to the console
 */
public class ConsoleManager extends JavaPlugin implements Listener
{
    private static final HashMap<String, Boolean> MINECRAFT = new HashMap<String, Boolean>();
    private static final HashMap<String, Boolean> BUKKIT    = new HashMap<String, Boolean>();
    private static final HashMap<String, Boolean> PLUGINS   = new HashMap<String, Boolean>();
    private static final HashMap<String, Boolean> SPECIFIC  = new HashMap<String, Boolean>();

    private static boolean keepDone;

    private ApacheFilter filter;
    private String       nms;

    /**
     * Whether or not to keep the "Done" message
     * after the server finishes loading
     *
     * @return true if should keep, false otherwise
     */
    public static boolean keepDone()
    {
        return keepDone;
    }

    /**
     * Checks if the server log should be allowed
     *
     * @param level logging level
     *
     * @return true if should display, false otherwise
     */
    public static boolean check(Level level)
    {
        String key = level.name().toLowerCase();
        return !MINECRAFT.containsKey(key) || MINECRAFT.get(key);
    }

    /**
     * Checks if the plugin log should be allowed
     *
     * @param level logging level
     *
     * @return true if allowed, false otherwise
     */
    public static boolean check(java.util.logging.Level level)
    {
        String key = level.getName().toLowerCase();
        return !BUKKIT.containsKey(key) || BUKKIT.get(key);
    }

    /**
     * Checks if the plugin log should be allowed
     *
     * @param plugin plugin name
     * @param level  logging level
     *
     * @return true if allowed, false otherwise
     */
    public static boolean check(String plugin, java.util.logging.Level level)
    {
        String key = level.getName().toLowerCase();
        if (SPECIFIC.containsKey(plugin + '_' + key))
            return SPECIFIC.get(plugin + '_' + key);
        return !PLUGINS.containsKey(key) || PLUGINS.get(key);
    }

    /**
     * Set up logging in the constructor so it happens as soon as possible
     */
    public ConsoleManager()
    {
        try
        {
            CommentedConfig config = new CommentedConfig(this, "config");
            config.checkDefaults();
            config.save();
            loadLevels(config.getConfig());

            Bukkit.getLogger().setUseParentHandlers(false);
            Bukkit.getLogger().addHandler(new JavaHandler());

            nms = getServer().getClass().getPackage().getName();
            nms = "net.minecraft.server" + nms.substring(nms.lastIndexOf('.')) + '.';
            filter = new ApacheFilter();
            keepDone = config.getConfig().getBoolean("keep-done-message");

            addFilter("WorldServer", "a");
            addFilter("MinecraftServer", "h");
            addFilter("DedicatedServer", "h");
        }
        catch (Exception ex)
        {
            getLogger().severe("Failed to set up logging utilities. Please notify Eniripsa96 with your server version");
            ex.printStackTrace();

        }
    }

    /**
     * Unregisters events
     */
    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Listener) this);
    }

    /**
     * Loads all logging level settings from the config data
     *
     * @param config config data to load from
     */
    private void loadLevels(DataSection config)
    {
        MINECRAFT.clear();
        BUKKIT.clear();
        PLUGINS.clear();
        SPECIFIC.clear();

        loadLevels(config.getSection("minecraft"), MINECRAFT);
        loadLevels(config.getSection("bukkit"), BUKKIT);
        loadLevels(config.getSection("plugins"), PLUGINS);

        DataSection specifics = config.getSection("specifics");
        for (String key : specifics.keys())
        {
            DataSection plugin = specifics.getSection(key);
            for (String type : plugin.keys())
                SPECIFIC.put(key + '_' + type, plugin.getBoolean(type));
        }
    }

    /**
     * Loads levels for one type of logging
     *
     * @param config config to load from
     * @param map    map to store the results
     */
    private void loadLevels(DataSection config, HashMap<String, Boolean> map)
    {
        for (String key : config.keys())
            map.put(key, config.getBoolean(key));
    }

    /**
     * Injects a filter into the logger for the given NMS server class
     *
     * @param name      NMS server class name
     * @param fieldName backup name of the field
     *
     * @throws Exception
     */
    private void addFilter(String name, String fieldName)
        throws Exception
    {
        Field staticLogger;
        org.apache.logging.log4j.core.Logger logger;
        try
        {
            staticLogger = Class.forName(nms + name)
                .getDeclaredField("LOGGER");
        }
        catch (Exception ex)
        {
            try
            {
                staticLogger = Class.forName(nms + name)
                    .getDeclaredField(fieldName);
            }
            catch (Exception ex2)
            {

                ex2.printStackTrace();
                return;
            }
        }
        staticLogger.setAccessible(true);
        logger = (org.apache.logging.log4j.core.Logger) staticLogger.get(null);
        logger.addFilter(filter);
    }
}
