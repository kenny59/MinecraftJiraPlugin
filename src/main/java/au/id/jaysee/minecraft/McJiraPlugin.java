package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.McJiraBlockListener;
import au.id.jaysee.minecraft.McJiraCommandExecutor;
import au.id.jaysee.minecraft.async.AsyncExecutor;
import au.id.jaysee.minecraft.jira.client.DefaultJiraClient;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URISyntaxException;
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

    /**
     * Invoked when the plugin is enabled and/or the server is started; perform initialisation here.
     */
    public void onEnable()
    {
        // TODO: print version
        log.info("Enabling Minecraft JIRA plugin - http://bitbucket.org/jaysee00/minecraftjiraplugin");

        // TODO: Load configuration

        // Load components
        jiraClient = new DefaultJiraClient(this, JIRA_BASE_URL, MINECRAFT_PROJECT_KEY, JIRA_ADMIN_USERNAME, JIRA_ADMIN_PASSWORD);
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
