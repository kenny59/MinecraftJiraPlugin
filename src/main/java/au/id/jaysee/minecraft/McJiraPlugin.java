package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.JiraIssuesHelper;
import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.config.ConfigurationLoader;
import au.id.jaysee.minecraft.jira.client.auth.DefaultAuthenticatedResourceFactory;
import au.id.jaysee.minecraft.task.TaskExecutor;
import com.atlassian.jira.rest.client.api.RestClientException;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.logging.Level;
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
        log.info("Plugin has been disabled because of fatal error");
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
            log.info("URISyntaxException");
        } catch (RestClientException e) {
            log.log(Level.SEVERE, "You probably tried to use password instead of token or your credentials are wrong. Please go to https://id.atlassian.com/manage/api-tokens to generate your token.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
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
