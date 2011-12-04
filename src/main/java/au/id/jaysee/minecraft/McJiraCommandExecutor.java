package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.jira.client.JiraClient;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class McJiraCommandExecutor implements CommandExecutor
{
    private final JavaPlugin plugin;
    private final JiraClient jiraClient;
    private final Logger log;

    private final Map<String, CommandExecutorImpl> executors = new HashMap<String, CommandExecutorImpl>();

    private static interface CommandExecutorImpl
    {
        public String getKey();

        public boolean execute(CommandSender sender, Command command, String label, String[] args);
    }


    public McJiraCommandExecutor(final JavaPlugin plugin, final JiraClient jiraClient, final Logger log)
    {
        this.plugin = plugin;
        this.jiraClient = jiraClient;
        this.log = log;
        registerExecutors();
    }

    private void registerExecutors()
    {
        CommandExecutorImpl jiraIssuesHandler = new CommandExecutorImpl()
        {
            @Override
            public String getKey()
            {
                return "jiraIssues";
            }

            @Override
            public boolean execute(CommandSender sender, Command command, String label, String[] args)
            {
                log.info("getissues invoked");
                jiraClient.printAllIssues();
                return true;
            }
        };
        executors.put(jiraIssuesHandler.getKey(), jiraIssuesHandler);

        CommandExecutorImpl gotoIssueHandler = new CommandExecutorImpl()
        {
            @Override
            public String getKey()
            {
                return "gotoIssue";
            }

            @Override
            public boolean execute(CommandSender sender, Command command, String label, String[] args)
            {
                log.info("where command used");
                if (args == null || args.length != 1)
                {
                    log.info("Incorrect args");

                    return false;
                }

                final String issueKey = args[0];
                Location l = jiraClient.getIssueLocation(issueKey);
                if (l == null)
                {
                    plugin.getServer().broadcastMessage(issueKey + " does not exist");
                    return true;
                }

                if (sender instanceof Player)
                {
                    Player player = (Player) sender;
                    player.teleport(l);

                } else
                {
                    log.info(issueKey + " is at " + l.toString());
                }
                return true;
            }
        };
        executors.put(gotoIssueHandler.getKey(), gotoIssueHandler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (executors.containsKey(command.getName()))
            return executors.get(command.getName()).execute(sender, command, label, args);

        return false;
    }
}
