package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.async.AsyncExecutor;
import au.id.jaysee.minecraft.async.Callback;
import au.id.jaysee.minecraft.async.Task;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import au.id.jaysee.minecraft.jira.client.JiraIssue;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class McJiraCommandExecutor implements CommandExecutor
{
    private final JavaPlugin plugin;
    private final JiraClient jiraClient;
    private final AsyncExecutor taskExecutor;
    private final Logger log;

    private final Map<String, CommandExecutorImpl> executors = new HashMap<String, CommandExecutorImpl>();

    private static interface CommandExecutorImpl
    {
        public String getKey();

        public boolean execute(CommandSender sender, Command command, String label, String[] args);
    }

    public McJiraCommandExecutor(final JavaPlugin plugin, final JiraClient jiraClient, final AsyncExecutor taskExecutor, final Logger log)
    {
        this.plugin = plugin;
        this.jiraClient = jiraClient;
        this.taskExecutor = taskExecutor;
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
            public boolean execute(final CommandSender sender, Command command, String label, String[] args)
            {
                log.info("getissues invoked");

                taskExecutor.executeAsyncTask(new Task<Collection<JiraIssue>>()
                    {
                        @Override
                        public Collection<JiraIssue> execute()
                        {
                            return jiraClient.getIssues();
                        }
                    }, new Callback<Collection<JiraIssue>>()
                    {
                        @Override
                        public void execute(Collection<JiraIssue> input)
                        {
                            if (sender instanceof Player)
                            {
                                Player p = (Player)sender;
                                for (JiraIssue j : input)
                                {
                                    // TODO: also print summary and maybe URL too.
                                    p.chat(j.getId());
                                }
                            }
                            else
                            {
                                for (JiraIssue j : input)
                                {
                                    // TODO: also print summary and maybe URL too.
                                    log.info(j.getId());
                                }
                            }
                        }
                    }
                );
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
