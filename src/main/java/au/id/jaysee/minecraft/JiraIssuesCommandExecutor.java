package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.jira.client.JiraClient;
import au.id.jaysee.minecraft.jira.client.JiraIssue;
import au.id.jaysee.minecraft.jira.client.JiraIssues;
import au.id.jaysee.minecraft.task.Callback;
import au.id.jaysee.minecraft.task.Task;
import au.id.jaysee.minecraft.task.TaskExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 *
 */
public class JiraIssuesCommandExecutor implements CommandExecutor
{
    private static final String COMMAND = "jiraIssues";

    private final TaskExecutor taskExecutor;
    private final Logger log;
    private final JiraClient jiraClient;

    public JiraIssuesCommandExecutor(final TaskExecutor taskExecutor, final Logger log, final JiraClient jiraClient)
    {
        this.taskExecutor = taskExecutor;
        this.log = log;
        this.jiraClient = jiraClient;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] strings)
    {
        if (!command.getName().equalsIgnoreCase(COMMAND))
            return false;

        log.info("jiraIssues command invoked");
        taskExecutor.executeAsyncTask(new Task<JiraIssues>()
        {
            @Override
            public JiraIssues execute()
            {
                return jiraClient.getIssues();
            }
        }, new Callback<JiraIssues>()
        {
            @Override
            public void execute(JiraIssues input)
            {
                if (sender instanceof Player)
                {
                    Player p = (Player)sender;
                    for (JiraIssue j : input.getIssues())
                    {
                        p.chat(j.getKey() + ": " + j.getSummary());
                    }
                }
                else
                {
                    for (JiraIssue j : input.getIssues())
                    {
                        log.info(j.getKey() + ": " + j.getSummary());
                    }
                }
            }
        });

        return true;
    }
}
