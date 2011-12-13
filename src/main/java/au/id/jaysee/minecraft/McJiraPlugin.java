package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.async.AsyncExecutor;
import au.id.jaysee.minecraft.jira.client.DefaultJiraClient;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Overall orchestrator and criminal mastermind of the Minecraft/JIRA plugin.
 *
 * @author Joe Clark
 */
public class McJiraPlugin extends JavaPlugin
{
    private static final Logger log = Logger.getLogger("Minecraft");

    // TODO: Replace with factory, creating client with plugged-in configuration values.
    private static final String JIRA_BASE_URL = "http://localhost:8080";
    private static final String MINECRAFT_PROJECT_KEY = "MC";
    private static final String JIRA_ADMIN_USERNAME = "admin";
    private static final String JIRA_ADMIN_PASSWORD = "admin";

    private JiraClient jiraClient;
    private AsyncExecutor taskExecutor;
    private CommandExecutor commandExecutor;
    private McJiraBlockListener blockListener;

    /**
     * Invoked when the plugin is disabled; perform tearDown/cleanup here.
     */
    public void onDisable()
    {
        log.info("Disabled message here, shown in console on startup");
        // TODO: graceful cleanup
    }

    private static final String JIRA_BASE_URL_KEY = "jira.base.url";
    private static final String MINECRAFT_PROJECT_KEY_KEY = "jira.minecraft.project.key";
    private static final String JIRA_ADMIN_USERNAME_KEY = "jira.admin.username";
    private static final String JIRA_ADMIN_PASSWORD_KEY = "jira.admin.password";
    private static final String LOCATION_CUSTOM_FIELD_KEY = "jira.location.custom.field";

    /**
     * Invoked when the plugin is enabled and/or the server is started; perform initialisation here.
     */
    public void onEnable()
    {
        // TODO: print version
        log.info("Enabling Minecraft JIRA plugin - http://bitbucket.org/jaysee00/minecraftjiraplugin");

        // TODO: Load configuration
        FileConfigurationOptions fileConfigurationOptions = getConfig().options().copyDefaults(true);
        saveConfig();

        FileConfiguration config = getConfig();
        String baseUrl = config.getString(JIRA_BASE_URL_KEY);
        if (StringUtils.isBlank(baseUrl))
            baseUrl = JIRA_BASE_URL;
        String minecraftProjectKey = config.getString(MINECRAFT_PROJECT_KEY_KEY);
        if (StringUtils.isBlank(minecraftProjectKey))
            minecraftProjectKey = MINECRAFT_PROJECT_KEY;
        String adminUsername = config.getString(JIRA_ADMIN_USERNAME_KEY);
        if (StringUtils.isBlank(adminUsername))
            adminUsername = JIRA_ADMIN_USERNAME;
        String adminPassword = config.getString(JIRA_ADMIN_PASSWORD_KEY);
        if (StringUtils.isBlank(adminPassword))
            adminPassword = JIRA_ADMIN_PASSWORD;

        log.info("base URL: " + baseUrl);

        // Load components
        jiraClient = new DefaultJiraClient(this, baseUrl, minecraftProjectKey, adminUsername, adminPassword);
        taskExecutor = new AsyncExecutor(this, getServer().getScheduler(), log);
        commandExecutor = new McJiraCommandExecutor(this, jiraClient, taskExecutor, log);
        blockListener = new McJiraBlockListener(this, jiraClient, taskExecutor, log);

        final PluginManager pluginManager = this.getServer().getPluginManager();
        // Register block event listeners - code that executes when the world environment is manipulated.
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);

        // Register command executors - code that executes in response to player /slash commands, or commands via the server console.
        // TODO: break into separate command executors
        getCommand("jiraIssues").setExecutor(commandExecutor);
        getCommand("gotoIssue").setExecutor(commandExecutor);
    }
}
