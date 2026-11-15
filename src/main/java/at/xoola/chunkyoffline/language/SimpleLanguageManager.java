package at.xoola.chunkyoffline.language;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SimpleLanguageManager {

    private final JavaPlugin plugin;
    private final Gson gson;
    private final Map<String, JsonObject> languages;
    private String defaultLanguage;

    public SimpleLanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.languages = new HashMap<>();
        this.defaultLanguage = "en";

        loadLanguages();
        loadConfig();
    }

    private void loadConfig() {
        String configLang = plugin.getConfig().getString("language", "de");
        if (languages.containsKey(configLang)) {
            this.defaultLanguage = configLang;
        } else {
            plugin.getLogger().warning(getMessage("language.not_found", configLang));
            this.defaultLanguage = "de";
        }
        plugin.getLogger().info(getMessage("language.using", this.defaultLanguage));
    }

    private void loadLanguages() {
        loadLanguageFromResource("en");
        loadLanguageFromResource("de");

        plugin.getLogger().info(getMessage("language.loaded", languages.size(), String.join(", ", languages.keySet())));
    }

    private void loadLanguageFromResource(String langCode) {
        try {
            InputStreamReader reader = new InputStreamReader(
                plugin.getResource("lang/" + langCode + ".json"),
                StandardCharsets.UTF_8
            );
            JsonObject langData = gson.fromJson(reader, JsonObject.class);
            languages.put(langCode, langData);
            reader.close();
        } catch (Exception e) {
            plugin.getLogger().warning(getMessage("language.load_error", langCode, e.getMessage()));
        }
    }

    public String getMessage(String key, Object... args) {
        return getMessageForLanguage(defaultLanguage, key, args);
    }

    public String getMessageForLanguage(String langCode, String key, Object... args) {
        JsonObject langData = languages.get(langCode);
        if (langData == null && !langCode.equals(defaultLanguage)) {
            langData = languages.get(defaultLanguage);
        }
        if (langData == null && !defaultLanguage.equals("en")) {
            langData = languages.get("en");
        }
        if (langData == null) {
            return "[MISSING LANG: " + key + "]";
        }
        String message = langData.has(key) ? langData.get(key).getAsString() : "[MISSING KEY: " + key + "]";
        if (args.length > 0) {
            try {
                message = MessageFormat.format(message, args);
            } catch (Exception e) {
                plugin.getLogger().warning(getMessage("language.format_error", key, e.getMessage()));
            }
        }

        return message;
    }

    public void sendMessage(CommandSender sender, String key, Object... args) {
        String message = getMessage(key, args);
        String prefix = getMessage("prefix");
        sender.sendMessage(prefix + " §f" + message);
    }

    public void sendPlayerMessage(Player player, String key, Object... args) {
        String message = getMessage(key, args);
        String prefix = getMessage("prefix");
        player.sendMessage(prefix + " §f" + message);
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
