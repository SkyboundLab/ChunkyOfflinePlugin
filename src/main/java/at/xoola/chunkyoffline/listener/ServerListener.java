package at.xoola.chunkyoffline.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;

import at.xoola.chunkyoffline.ChunkyOfflinePlugin;
import at.xoola.chunkyoffline.config.ConfigManager;
import at.xoola.chunkyoffline.manager.ChunkyManager;

public class ServerListener implements Listener {

    private final ChunkyOfflinePlugin plugin;
    private final ConfigManager configManager;
    private final ChunkyManager chunkyManager;

    public ServerListener(ChunkyOfflinePlugin plugin, ConfigManager configManager, ChunkyManager chunkyManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.chunkyManager = chunkyManager;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        try {
            if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
                return;
            }
        } catch (NoSuchMethodError e) {
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("plugin.startup_detection"));
        }
        plugin.getLogger().info(plugin.getLanguageManager().getMessage("plugin.ready"));
        plugin.getLogger().info(plugin.getLanguageManager().getMessage("plugin.ready.use"));
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("chunkyoffline.admin.info")) {
                plugin.getLanguageManager().sendPlayerMessage(player, "plugin.ready");
                plugin.getLanguageManager().sendPlayerMessage(player, "plugin.ready.use");
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("chunkyoffline.admin.info")) {
            boolean autoMode = chunkyManager.isAutoModeEnabled();
            boolean generating = chunkyManager.isGenerating();

            if (autoMode) {
                if (generating) {
                    plugin.getLanguageManager().sendPlayerMessage(event.getPlayer(), "status.will_pause");
                } else {
                    plugin.getLanguageManager().sendPlayerMessage(event.getPlayer(), "status.auto_enabled");
                }
            } else {
                plugin.getLanguageManager().sendPlayerMessage(event.getPlayer(), "status.auto_disabled");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (chunkyManager.isAutoModeEnabled()) {
            plugin.getLogger().info(plugin.getLanguageManager().getMessage("player.left", event.getPlayer().getName()));
        }
    }
}
