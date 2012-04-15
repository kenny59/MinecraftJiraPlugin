package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.jira.client.ActivityStreamClient;
import au.id.jaysee.minecraft.jira.client.JiraUserClient;
import au.id.jaysee.minecraft.task.Task;
import au.id.jaysee.minecraft.task.TaskExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 *
 */
public class LoginListener implements Listener
{
    private final Logger log;
    private final ActivityStreamClient activityStreamClient;
    private final JiraUserClient jiraUserClient;
    private final TaskExecutor taskExecutor;
    private final Configuration pluginConfig;

    LoginListener(Plugin plugin, Configuration pluginConfig, JiraUserClient jiraUserClient, ActivityStreamClient activityStreamClient, TaskExecutor taskExecutor)
    {
        this.activityStreamClient = activityStreamClient;
        this.jiraUserClient = jiraUserClient;
        this.taskExecutor = taskExecutor;
        this.pluginConfig = pluginConfig;
        log = plugin.getLogger();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuitEvent(final PlayerQuitEvent event)
    {
        taskExecutor.executeAsyncTask(new Task<Object>()
        {
            @Override
            public Object execute()
            {
                activityStreamClient.postActivity(event.getPlayer(), "<strong>" + event.getPlayer().getDisplayName() + "</strong> logged out of Minecraft", "<blockquote>" + event.getQuitMessage() + "</blockquote>");
                return null;
            }
        });
    }

    @EventHandler
    public void onPlayerJoinEvent(final PlayerJoinEvent event)
    {
        taskExecutor.executeAsyncTask(new Task<Object>()
        {
            @Override
            public Object execute()
            {
                if (pluginConfig.isDynamicUserCreationEnabled())
                {
                    if (!jiraUserClient.doesJiraUserExist(event.getPlayer().getName()))
                    {
                        jiraUserClient.createUser(event.getPlayer().getName());
                    }
                }

                activityStreamClient.postActivity(event.getPlayer(), "<strong>" + event.getPlayer().getDisplayName() + "</strong> logged in to Minecraft", "<blockquote>" + event.getJoinMessage() + "</blockquote>");
                return null;
            }
        });
    }


}
