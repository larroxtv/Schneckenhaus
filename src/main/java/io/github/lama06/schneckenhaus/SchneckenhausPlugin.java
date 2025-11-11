package io.github.lama06.schneckenhaus;

import io.github.lama06.schneckenhaus.command.SchneckenhausCommand;
import io.github.lama06.schneckenhaus.config.ConfigManager;
import io.github.lama06.schneckenhaus.config.SchneckenhausConfig;
import io.github.lama06.schneckenhaus.database.DatabaseManager;
import io.github.lama06.schneckenhaus.language.Language;
import io.github.lama06.schneckenhaus.language.Translator;
import io.github.lama06.schneckenhaus.legacy.LegacyImporter;
import io.github.lama06.schneckenhaus.shell.ShellManager;
import io.github.lama06.schneckenhaus.shell.custom.CustomShell;
import io.github.lama06.schneckenhaus.systems.Systems;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.sql.Connection;

public final class SchneckenhausPlugin extends JavaPlugin implements Listener {
    public static SchneckenhausPlugin INSTANCE;

    private Logger logger;
    private ConfigManager config;
    private Translator translator;
    private DatabaseManager database;
    private WorldManager worlds;
    private ShellManager shellManager;
    private SchneckenhausCommand command;
    private Systems systems;


    public SchneckenhausPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        try {
            Files.createDirectories(getDataPath().resolve(CustomShell.IMPORT_DIRECTORY));
            Files.createDirectories(getDataPath().resolve(CustomShell.EXPORT_DIRECTORY));

            config = new ConfigManager();
            if (!config.load()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            database = new DatabaseManager();
            if (!database.connect()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            translator = new Translator();
            translator.loadConfig();

            worlds = new WorldManager();
            worlds.load();

            shellManager = new ShellManager();

            Permission.register();

            systems = new Systems();
            systems.start();

            command = new SchneckenhausCommand();
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> command.register(event.registrar()));

            Bukkit.getPluginManager().registerEvents(this, this);

            new LegacyImporter().loadLegacyDataIfNecessary();
        } catch (Exception e) {
            getSLF4JLogger().error("failed to enable Schneckenhaus plugin", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public Logger getSLF4JLogger() {
        // Cache the logger because it is somewhat expensive to create a new one
        if (logger == null) {
            logger = super.getSLF4JLogger();
        }
        return logger;
    }

    public SchneckenhausConfig getPluginConfig() {
        if (config == null) {
            return null; // during init, called by ConstantsHolder class
        }
        return config.getConfig();
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public ShellManager getShellManager() {
        return shellManager;
    }

    public Translator getTranslator() {
        return translator;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public Connection getDatabaseConnection() {
        if (database == null) {
            return null; // during init
        }
        return database.getConnection();
    }

    public SchneckenhausCommand getCommand() {
        return command;
    }

    public Systems getSystems() {
        return systems;
    }
}
