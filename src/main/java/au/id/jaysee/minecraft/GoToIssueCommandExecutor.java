package au.id.jaysee.minecraft;

import au.id.jaysee.minecraft.jira.client.DefaultJiraClient;
import au.id.jaysee.minecraft.jira.client.IssueLocation;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import au.id.jaysee.minecraft.task.TaskExecutor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 *
 */
public class GoToIssueCommandExecutor implements CommandExecutor
{
    private static final String COMMAND = "gotoIssue";

    private final JavaPlugin parentPlugin;
    private final Logger log;
    private final JiraClient jiraClient;

    public GoToIssueCommandExecutor(final JavaPlugin parentPlugin, final Logger log, final JiraClient jiraClient)
    {
        this.log = log;
        this.jiraClient = jiraClient;
        this.parentPlugin = parentPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!command.getName().equalsIgnoreCase(COMMAND))
            return false;

        log.info("gotoIssue command invoked");
        if (args == null || args.length != 1)
        {
            log.info("gotoIssue command invoked with incorrect arguments. Expected 'gotoIssue {issueKey}'");
            return false;
        }

        final String issueKey = args[0];
        IssueLocation issueLocation = jiraClient.getIssueLocation(issueKey);
        if (issueLocation == null)
        {
            parentPlugin.getServer().broadcastMessage(issueKey + " does not exist");
            return true;
        }

        World world = parentPlugin.getServer().getWorld(issueLocation.getWorld());
        Location l = new Location(world, issueLocation.getX(), issueLocation.getY(), issueLocation.getZ());

        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            l = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

            player.teleport(l);

        }
        else
        {
            log.info(issueKey + " is at " + l.toString());
        }
        return true;
    }
}

