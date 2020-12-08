package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.JiraIssuesHelper;
import au.id.jaysee.utils.EnumUtils;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class GoToIssueCommandExecutor implements CommandExecutor
{
    private static final String COMMAND = "gotoIssue";

    private final JavaPlugin parentPlugin;
    private final Logger log;
    private final JiraIssuesHelper jiraClient;

    public GoToIssueCommandExecutor(final JavaPlugin parentPlugin, final Logger log, final JiraIssuesHelper jiraClient)
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
        Issue issueLocation = jiraClient.getIssueByKey(issueKey);
        if (issueLocation == null)
        {
            parentPlugin.getServer().broadcastMessage(issueKey + " does not exist");
            return true;
        }

        List<Field> fields = jiraClient.getFields();

        Field w = jiraClient.getCustomFieldIdByName(fields, "World");
        Field x = jiraClient.getCustomFieldIdByName(fields, "X");
        Field y = jiraClient.getCustomFieldIdByName(fields, "Y");
        Field z = jiraClient.getCustomFieldIdByName(fields, "Z");


        World world = sender.getServer().getWorld(issueLocation.getField(w.getId()).getValue().toString());
        Location l = getTeleportableLocation(world, Double.parseDouble(issueLocation.getField(x.getId()).getValue().toString()),
                Double.parseDouble(issueLocation.getField(y.getId()).getValue().toString()),
                Double.parseDouble(issueLocation.getField(z.getId()).getValue().toString()));

        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if(l != null) {
                l = new Location(l.getWorld(), l.getBlockX()+0.5, l.getBlockY(), l.getBlockZ()+0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

                l.getChunk().load(true);
                player.teleport(l);
            } else {
                sender.sendMessage("Location is not safe for teleport");
            }
        }
        else
        {
            log.info(issueKey + " is at " + l.toString());
        }
        return true;
    }

    Location getTeleportableLocation(World world, double x, double y, double z) {
        Location location = new Location(world, x, y, z);

        if(playerFitsAndSolidBlockUnderneath(location)) {
            return location;
        }
        for(int h = -1; h <= 0; h++) {
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    if(playerFitsAndSolidBlockUnderneath(location.getBlock().getRelative(i, h, j).getLocation())) {
                        return location.getBlock().getRelative(i, h, j).getLocation();
                    }
                }
            }
        }
        return null;
    }

    boolean playerFitsAndSolidBlockUnderneath(Location location) {
        return ((location.getBlock().getType().isAir() || EnumUtils.isSign(location.getBlock().getType().name())) && location.getBlock().getRelative(0, 1, 0).getType().isAir() && location.getBlock().getRelative(0, -1, 0).getType().isSolid());
    }
}

