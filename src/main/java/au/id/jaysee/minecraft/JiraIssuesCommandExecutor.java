package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.JiraIssuesHelper;
import au.id.jaysee.minecraft.task.Callback;
import au.id.jaysee.minecraft.task.Task;
import au.id.jaysee.minecraft.task.TaskExecutor;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class JiraIssuesCommandExecutor implements CommandExecutor
{
    private static final String COMMAND = "jiraIssues";

    private final TaskExecutor taskExecutor;
    private final Logger log;
    private final JiraIssuesHelper jiraClient;

    public JiraIssuesCommandExecutor(final TaskExecutor taskExecutor, final Logger log, final JiraIssuesHelper jiraClient)
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

        taskExecutor.executeAsyncTask(new Task<List<Issue>>()
        {
            @Override
            public List<Issue> execute() {
                return jiraClient.getIssues();
            }
        }, new Callback<List<Issue>>()
        {

            @Override
            public void execute(List<Issue> input)
            {
                if (sender instanceof Player)
                {
                    Player p = (Player)sender;
                    for (Issue j : input)
                    {
                        p.sendMessage(j.getKey() + ": " + j.getSummary());
                    }
                }
                else
                {
                    for (Issue j : input)
                    {
                        log.info(j.getKey() + ": " + j.getSummary());
                    }
                }
            }
        });

        return true;
    }
}
