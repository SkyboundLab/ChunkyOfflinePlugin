package at.xoola.chunkyoffline.manager;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import at.xoola.chunkyoffline.ChunkyOfflinePlugin;
import at.xoola.chunkyoffline.config.ConfigManager;

public class ChunkyManager {

    private final ChunkyOfflinePlugin plugin;
    private final ConfigManager configManager;
    private final AtomicBoolean isGenerating = new AtomicBoolean(false);
    private final AtomicBoolean autoModeEnabled = new AtomicBoolean(false);
    private Plugin chunkyPlugin;

    public ChunkyManager(ChunkyOfflinePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.initializeChunkyPlugin();
    }

    private void initializeChunkyPlugin() {
        try {
            this.chunkyPlugin = Bukkit.getPluginManager().getPlugin("Chunky");
            if (chunkyPlugin == null) {
                plugin.getLogger().severe(plugin.getLanguageManager().getMessage("error.chunky_not_found"));
            } else {
                plugin.getLogger().info(plugin.getLanguageManager().getMessage("error.chunky_found", chunkyPlugin.getPluginMeta().getVersion()));
            }
        } catch (Exception e) {
            plugin.getLogger().severe(plugin.getLanguageManager().getMessage("error.chunky_init_failed", e.getMessage()));
        }
    }

    public void start() {
        if (chunkyPlugin == null || !chunkyPlugin.isEnabled()) {
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("error.chunky_not_available"));
            return;
        }

        if (isGenerating.compareAndSet(false, true)) {
            autoModeEnabled.set(true);
            int radius = configManager.getRadius();
            int x = configManager.getCenterX();
            int z = configManager.getCenterZ();
            executeChunkyCommandsDirectly(x, z, radius);
        } else {
            broadcastMessage(plugin.getLanguageManager().getMessage("chunk.already_running"));
        }
    }

    public void startInternal() {
        if (chunkyPlugin == null || !chunkyPlugin.isEnabled()) {
            plugin.getLogger().fine(plugin.getLanguageManager().getMessage("error.chunky_periodic_unavailable"));
            return;
        }

        if (isGenerating.compareAndSet(false, true)) {
            try {
                plugin.getLogger().info(plugin.getLanguageManager().getMessage("monitor.resume_periodic"));
                plugin.getLogger().info(plugin.getLanguageManager().getMessage("monitor.auto_starting"));

                isGenerating.set(false);
            } catch (Exception e) {
                plugin.getLogger().warning(plugin.getLanguageManager().getMessage("monitor.error_periodic", e.getMessage()));
                isGenerating.set(false);
            }
        }
    }

    private void executeChunkyCommandsDirectly(int x, int z, int radius) {
        String world = "world";
        isGenerating.set(true);
        autoModeEnabled.set(true);
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky pause");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky cancel");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky confirm");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + world);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky center " + x + " " + z);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky radius " + radius);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky start");

            broadcastMessage(plugin.getLanguageManager().getMessage("chunk.started", x, z, radius));
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("chunk.started", x, z, radius));
        });
    }

    public void pause() {
        if (chunkyPlugin == null || !chunkyPlugin.isEnabled()) {
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("error.chunky_not_available"));
            return;
        }

        isGenerating.set(false);

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky pause");
            broadcastMessage(plugin.getLanguageManager().getMessage("chunk.paused"));
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("chunk.paused"));
        });
    }

    public void resume() {
        if (chunkyPlugin == null || !chunkyPlugin.isEnabled()) {
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("error.chunky_not_available"));
            return;
        }

        isGenerating.set(true);
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky continue");
            broadcastMessage(plugin.getLanguageManager().getMessage("chunk.resumed"));
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("chunk.resumed"));
        }, 20L);
    }

    public void cancel() {
        if (chunkyPlugin == null || !chunkyPlugin.isEnabled()) {
            plugin.getLogger().warning(plugin.getLanguageManager().getMessage("error.chunky_not_available"));
            return;
        }

        isGenerating.set(false);
        autoModeEnabled.set(false);

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky pause");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky cancel");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chunky confirm");
            broadcastMessage(plugin.getLanguageManager().getMessage("chunk.cancelled"));
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("chunk.cancelled"));
        });
    }

    public boolean isAutoModeEnabled() {
        return autoModeEnabled.get();
    }

    public boolean shouldRunBasedOnPlayerCount() {
        return plugin.getServer().getOnlinePlayers().isEmpty();
    }

    private void broadcastMessage(String message) {
        String prefix = plugin.getLanguageManager().getMessage("prefix");
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("chunkyoffline.admin.info")) {
                player.sendMessage(prefix + " §f" + message);
            }
        });
        plugin.getLogger().info(message);
    }

    public boolean isGenerating() {
        return isGenerating.get();
    }

    public void setGenerating(boolean generating) {
        isGenerating.set(generating);
    }

    public Plugin getChunkyPlugin() {
        return chunkyPlugin;
    }
}
