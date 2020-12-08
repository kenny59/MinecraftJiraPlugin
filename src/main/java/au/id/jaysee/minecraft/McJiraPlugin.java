package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.JiraIssuesHelper;
import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.config.ConfigurationLoader;
import au.id.jaysee.minecraft.jira.client.auth.DefaultAuthenticatedResourceFactory;
import au.id.jaysee.minecraft.task.TaskExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Overall orchestrator and criminal mastermind of the Minecraft/JIRA plugin.
 *
 * @author Joe Clark
 */
public class McJiraPlugin extends JavaPlugin
{
    private Logger log;

    private JiraIssuesHelper jiraClient;
    private TaskExecutor taskExecutor;
    private McJiraBlockListener blockListener;

    /**
     * Invoked when the plugin is disabled; perform tearDown/cleanup here.
     */
    public void onDisable()
    {
        log.info("Disabled message here, shown in console on startup");
    }

    /**
     * Invoked when the plugin is enabled and/or the server is started; perform initialisation here.
     */
    public void onEnable()
    {
        log = getLogger();
        log.info("Enabling Minecraft JIRA plugin");

        final Configuration config = loadConfiguration();

        DefaultAuthenticatedResourceFactory resourceFactory = null;
        try {
            resourceFactory = new DefaultAuthenticatedResourceFactory(config, log);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        jiraClient = new JiraIssuesHelper(resourceFactory.getClient(), config);

        taskExecutor = new TaskExecutor(this, getServer().getScheduler());

        blockListener = new McJiraBlockListener(this, jiraClient, taskExecutor, log, config);

        final PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(blockListener, this);

        getCommand("jiraIssues").setExecutor(new JiraIssuesCommandExecutor(taskExecutor, log, jiraClient));
        getCommand("gotoIssue").setExecutor(new GoToIssueCommandExecutor(this, log, jiraClient));
    }

    private Configuration loadConfiguration()
    {
        saveDefaultConfig();

        final ConfigurationLoader loader = new ConfigurationLoader(getConfig(), log);
        return loader.load();
    }
}
