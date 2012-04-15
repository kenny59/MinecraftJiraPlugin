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

    // Config file keys
    private static final String JIRA_BASE_URL_KEY = "jira.base.url";
    private static final String MINECRAFT_PROJECT_KEY = "jira.minecraft.project.key";
    private static final String JIRA_ADMIN_USERNAME_KEY = "jira.admin.username";
    private static final String JIRA_ADMIN_PASSWORD_KEY = "jira.admin.password";
    private static final String LOCATION_CUSTOM_FIELD_KEY = "jira.location.custom.field";
    private static final String DEBUG_LOGGING_ENABLED_KEY = "debug.logging.enabled";

    private static final String DYNAMIC_USER_CREATION_ENABLED_KEY = "enable.feature.createUsers";

    // Defaults
    private static final String DEFAULT_JIRA_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_MINECRAFT_PROJECT_KEY = "MC";
    private static final String DEFAULT_JIRA_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_JIRA_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_LOCATION_CUSTOM_FIELD = "10000";
    private static final boolean DEFAULT_DEBUG_LOGGING_ENABLED = false;
    private static final boolean DEFAULT_DYNAMIC_USER_CREATION_ENABLED = true;

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

    /**
     * Loads the configuration from disk and returns the values in a strongly-typed {@link Configuration} object.
     */
    public Configuration load()
    {
        log.info("Loading Minecraft JIRA Plugin configuration.");
        return new Configuration(
                configFile.getString(JIRA_BASE_URL_KEY, DEFAULT_JIRA_BASE_URL),
                configFile.getString(MINECRAFT_PROJECT_KEY, DEFAULT_MINECRAFT_PROJECT_KEY),
                configFile.getString(JIRA_ADMIN_USERNAME_KEY, DEFAULT_JIRA_ADMIN_USERNAME),
                configFile.getString(JIRA_ADMIN_PASSWORD_KEY, DEFAULT_JIRA_ADMIN_PASSWORD),
                configFile.getString(LOCATION_CUSTOM_FIELD_KEY, DEFAULT_LOCATION_CUSTOM_FIELD),
                configFile.getBoolean(DEBUG_LOGGING_ENABLED_KEY, DEFAULT_DEBUG_LOGGING_ENABLED),
                configFile.getBoolean(DYNAMIC_USER_CREATION_ENABLED_KEY, DEFAULT_DYNAMIC_USER_CREATION_ENABLED)
        );
    }
}
