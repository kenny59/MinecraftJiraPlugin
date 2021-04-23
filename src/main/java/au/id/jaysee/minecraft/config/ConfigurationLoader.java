package au.id.jaysee.minecraft.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

/**
 * Loads configuration details for the Minecraft JIRA Plugin from a configuration file in the Bukkit server's plugins
 * directory. A file containing default configurations is created when the plugin is first loaded. If the file is not
 * present, hard-coded defaults are used.
 *
 * @author Joe Clark
 * @see <a href="http://wiki.bukkit.org/Introduction_to_the_New_Configuration">Bukkit Wiki: Introduction to the New Configuration</a>
 * @since 1.0
 */
public class ConfigurationLoader
{
    private final FileConfiguration configFile;
    private final Logger log;

    /**
     * Constructs the ConfigurationLoader service.
     *
     * @param configFile A reference to the plugin's config file, provided by the Bukkit API.
     * @param log        Logger for printing information to the server console.
     */
    public ConfigurationLoader(final FileConfiguration configFile, final Logger log)
    {
        this.configFile = configFile;
        this.log = log;
    }

    public Configuration load()
    {
        log.info("Loading Minecraft JIRA Plugin configuration.");
        return new Configuration(
                configFile.getString("jira.base.url"),
                configFile.getString("jira.minecraft.project.key"),
                configFile.getString("jira.minecraft.project.parent"),
                configFile.getString("jira.admin.username"),
                configFile.getString("jira.admin.api.token"),
                configFile.getBoolean("debug.logging.enabled"),
                configFile.getBoolean("true")
        );
    }
}
