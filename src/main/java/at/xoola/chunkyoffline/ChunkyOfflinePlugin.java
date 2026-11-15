package at.xoola.chunkyoffline;

import org.bukkit.plugin.java.JavaPlugin;

import at.xoola.chunkyoffline.commands.MainCommandExecutor;
import at.xoola.chunkyoffline.config.ConfigManager;
import at.xoola.chunkyoffline.language.SimpleLanguageManager;
import at.xoola.chunkyoffline.listener.ServerListener;
import at.xoola.chunkyoffline.manager.ChunkyManager;

public class ChunkyOfflinePlugin extends JavaPlugin {

    private static ChunkyOfflinePlugin instance;
    private ConfigManager configManager;
    private ChunkyManager chunkyManager;
    private SimpleLanguageManager languageManager;
    private boolean periodicTaskRunning = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.languageManager = new SimpleLanguageManager(this);
        this.configManager = new ConfigManager(this);
        this.chunkyManager = new ChunkyManager(this, configManager);
        registerCommands();
        registerListeners();
        startPeriodicTask();

        getLogger().info(languageManager.getMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        periodicTaskRunning = false;
        getLogger().info(languageManager.getMessage("plugin.disabled"));
    }

    private void registerCommands() {
        org.bukkit.command.PluginCommand cmd = getCommand("chunkyoffline");
        if (cmd != null) {
            MainCommandExecutor mainCommand = new MainCommandExecutor(this, configManager, chunkyManager);
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ServerListener(this, configManager, chunkyManager), this);
    }

    private void startPeriodicTask() {
        periodicTaskRunning = true;
        Thread.ofVirtual().name("ChunkyOffline-Monitor").start(() -> {
            getLogger().info(languageManager.getMessage("monitor.started"));

            while (periodicTaskRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);

                    if (!periodicTaskRunning) break;
                    performDatapackStyleCheck();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    getLogger().info(languageManager.getMessage("monitor.interrupted"));
                    break;
                } catch (Exception e) {
                    getLogger().warning(languageManager.getMessage("monitor.error", e.getMessage()));
                }
            }

            getLogger().info(languageManager.getMessage("monitor.stopped"));
        });
    }

    private void performDatapackStyleCheck() {
        getServer().getGlobalRegionScheduler().run(this, (task) -> {
            try {
                boolean hasPlayers = !getServer().getOnlinePlayers().isEmpty();
                boolean autoModeEnabled = chunkyManager.isAutoModeEnabled();
                boolean currentlyGenerating = chunkyManager.isGenerating();
                if (!autoModeEnabled) {
                    return;
                }

                if (currentlyGenerating && hasPlayers) {
                    chunkyManager.pause();
                    return;
                }
                if (!currentlyGenerating && !hasPlayers) {
                    chunkyManager.resume();
                    return;
                }

            } catch (Exception e) {
                getLogger().warning(languageManager.getMessage("error.datapack_check", e.getMessage()));
            }
        });
    }

    public static ChunkyOfflinePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChunkyManager getChunkyManager() {
        return chunkyManager;
    }

    public SimpleLanguageManager getLanguageManager() {
        return languageManager;
    }
}
