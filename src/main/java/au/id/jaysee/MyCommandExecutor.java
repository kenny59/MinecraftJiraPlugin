package au.id.jaysee;

/*
    This file is part of ${artifactId}

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

public class MyCommandExecutor implements CommandExecutor {

    private McJiraPlugin plugin;
    private final JiraService jira;
    Logger log = Logger.getLogger("Minecraft");//Define your logger

    public MyCommandExecutor(McJiraPlugin plugin, JiraService jira) {
        this.plugin = plugin;
        this.jira = jira;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        log.info("onCommand Reached in McJiraPlugin");

        if (command.getName().equalsIgnoreCase("command")) {
	        log.info("command used");
            Player player = (Player) sender;
			player.setHealth(100);
            return true;
        }

        if (command.getName().equalsIgnoreCase("getissues"))
        {
            log.info("getissues invoked");
            jira.PrintAllIssues();
        }

        if (command.getName().equalsIgnoreCase("where"))
        {
            log.info("where command used");
            if (args == null || args.length != 1)
            {
                log.info("Incorrect args");

                return true;
            }

            final String issueKey = args[0];
            Location l = jira.getIssueLocation(issueKey);
            if (l == null)
            {
                plugin.getServer().broadcastMessage(issueKey + " does not exist");
                return true;
            }

            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                long round = Math.round(l.distance(player.getLocation()));

                player.teleport(l);

//                plugin.getServer().broadcastMessage("You're about " + round + " blocks away from " + issueKey);
//
//                Vector vectorToTravel = l.toVector().subtract(player.getLocation().toVector());
//                vectorToTravel = vectorToTravel.normalize();
//                Vector vectorCurrentlyTravelling = player.getLocation().getDirection().normalize();
//
//                //Vector adjustmentsNeeded = vectorToTravel.subtract(vectorCurrentlyTravelling).normalize();
//
//                Location adjustedLocation = player.getLocation();
//                float adjustedPitch = new Double(Math.sqrt(Math.pow(vectorToTravel.getX(), 2) + Math.pow(vectorToTravel.getY(), 2)) / vectorToTravel.getZ()).floatValue();
//                float adjustedYaw = new Double(Math.tan(vectorToTravel.getX() / (vectorToTravel.getY() * -1))).floatValue();
//                adjustedLocation.setPitch(adjustedPitch);
//                adjustedLocation.setYaw(adjustedYaw);
//
//                player.teleport(adjustedLocation);
            }
            else
            {
                log.info(issueKey + " is at " + l.toString());
            }

        }


        return true;
    }
}
