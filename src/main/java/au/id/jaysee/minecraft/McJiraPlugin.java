package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.jira.client.auth.AuthenticatedResourceFactory;
import au.id.jaysee.minecraft.jira.client.auth.DefaultAuthenticatedResourceFactory;
import au.id.jaysee.minecraft.task.TaskExecutor;
import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.config.ConfigurationLoader;
import au.id.jaysee.minecraft.jira.client.DefaultJiraClient;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.EventExecutor;
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
    private Logger log;

    private JiraClient jiraClient;
    private TaskExecutor taskExecutor;
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
        log = getLogger();
        log.info("Enabling Minecraft JIRA plugin - http://bitbucket.org/jaysee00/minecraftjiraplugin");

        // Load plugin configuration from config.yml
        final Configuration config = loadConfiguration();

        // Use the configuration to login to Jira.
        AuthenticatedResourceFactory resourceFactory = new DefaultAuthenticatedResourceFactory(config, log);
        if (!resourceFactory.login())
        {
            log.severe("*********************************************************");
            log.severe("* Unable to login in to JIRA. Check your configuration. *");
            log.severe("*********************************************************");
        }

        // Load components
        jiraClient = new DefaultJiraClient(log, resourceFactory, config.getLocationCustomFieldId(), config.getMinecraftProjectKey(), config.getJiraAdminUsername());
        taskExecutor = new TaskExecutor(this, getServer().getScheduler());

        blockListener = new McJiraBlockListener(this, jiraClient, taskExecutor, log, config);

        // Register block event listeners - code that executes when the world environment is manipulated.
        final PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(blockListener, this);

        // Register command executors - code that executes in response to player /slash commands, or commands via the server console.
        getCommand("jiraIssues").setExecutor(new JiraIssuesCommandExecutor(taskExecutor, log, jiraClient));
        getCommand("gotoIssue").setExecutor(new GoToIssueCommandExecutor(this, log, jiraClient));
    }

    private Configuration loadConfiguration()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();

        final ConfigurationLoader loader = new ConfigurationLoader(getConfig(), log);
        return loader.load();
    }
}
