package au.id.jaysee;

import au.id.jaysee.jira.client.JiraSoapService;
import au.id.jaysee.jira.client.JiraSoapServiceServiceLocator;
import au.id.jaysee.jira.client.RemoteComment;
import au.id.jaysee.jira.client.RemoteFieldValue;
import au.id.jaysee.jira.client.RemoteIssue;
import au.id.jaysee.jira.client.RemoteIssueType;
import au.id.jaysee.jira.client.RemoteProject;
import au.id.jaysee.minecraft.McJiraPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @deprecated Use {@link au.id.jaysee.minecraft.jira.client.JiraClient} instead.
 */
public class JiraService
{
    private final Logger log = Logger.getLogger("Minecraft");

    private static final String JIRA_BASE_URL = "http://localhost:9090"; // TODO: Make this configurable.
    private static final String MINECRAFT_PROJECT_KEY = "MC"; // TODO: Make this configurable.
    private static final String JIRA_ENDPOINT_FORMAT = "%s/rpc/soap/jirasoapservice-v2";


    private final Map<String, Location> issues = new HashMap<String, Location>();

    private final McJiraPlugin plugin;

    public JiraService(McJiraPlugin plugin)
    {
        this.plugin = plugin;
    }

    private JiraSoapService service;
    private String token;

    private boolean primeSoapClient()
    {
        if (service == null)
        {
            JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
            locator.setJirasoapserviceV2EndpointAddress(String.format(JIRA_ENDPOINT_FORMAT, JIRA_BASE_URL));
            try
            {
                service = locator.getJirasoapserviceV2();
            }
            catch (ServiceException e)
            {
                log.info("Unable to create JIRA Client: " + e.toString());
                return false;
            }

            try
            {
                token = service.login("admin", "admin");
            }
            catch (RemoteException e)
            {
                log.info("Unable to login to JIRA: " + e.toString());
                return false;
            }
        }
        return true;
    }

    public void ResolveIssueAsync(final String issueKey, final String player)
    {
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                if (!primeSoapClient())
                    return;

                try
                {
                    final String workflowAction = "5"; // 5 == Resolve
                    final String resolutionType = "1"; // 1= Fixed
                    RemoteIssue issue = service.progressWorkflowAction(token, issueKey, workflowAction, new RemoteFieldValue[]{new RemoteFieldValue("resolution", new String[]{resolutionType})});

                    RemoteComment c = new RemoteComment();
                    c.setAuthor("admin");
                    c.setBody("Issue resolved by Minecraft User " + player);
                    service.addComment(token, issueKey, c);

                    if (issues.containsKey(issueKey))
                        issues.remove(issueKey);

                    // Schedule a sync task to broadcast a message that the issue has been resolved.
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            plugin.getServer().broadcastMessage("Resolved JIRA Issue " + issueKey);
                        }
                    });
                }
                catch (Exception e)
                {
                    log.info(e.getMessage());
                    log.info("Uh-oh! " + e.toString());
                }
            }
        });
    }

    public Location getIssueLocation(String issueKey)
    {
        if (issues.containsKey(issueKey))
        {
            return issues.get(issueKey);
        }
        return null;
    }

    public void PrintAllIssues()
    {
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
        {

            @Override
            public void run()
            {
                if (!primeSoapClient())
                    return;
                try
                {
                    log.info("getting JIRA issues");
                    RemoteIssue[] issuesFromJqlSearch = service.getIssuesFromJqlSearch(token, "project = \"MC\" and resolution = unresolved", 10);
                    log.info("Found " + issuesFromJqlSearch.length + " issues");

                    final List<String> broadcastMessage = new ArrayList<String>();
                    broadcastMessage.add("All Available Minecraft Issues:\n");
                    for (RemoteIssue issue : issuesFromJqlSearch)
                    {
                        broadcastMessage.add("{" + issue.getKey() + "} " + issue.getSummary());
                    }
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (String s : broadcastMessage)
                            {
                                plugin.getServer().broadcastMessage(s);
                            }
                        }

                    });
                }
                catch (Exception e)
                {
                    log.info(e.getMessage());
                    log.info("Uh-oh! " + e.toString());
                    return;
                }
            }
        });
    }


    public void CreateIssueAsync(final String creator, final String text, final int x, final int y, final int z)
    {
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                final String issueKey;
                if (!primeSoapClient())
                    return;
                try
                {
                    // TODO: Ensure that the project exists, create it if not?
                    RemoteProject mc = service.getProjectByKey(token, "MC");
                    RemoteIssueType[] issueTypes = service.getIssueTypesForProject(token, mc.getId());
                    String issueTypeId = "";
                    for (RemoteIssueType t : issueTypes)
                    {
                        if ("Bug".equals(t.getName()))
                        {
                            issueTypeId = t.getId();
                            break;
                        }
                    }

                    RemoteIssue ri = new RemoteIssue();
                    ri.setProject("MC");
                    ri.setReporter("admin");
                    ri.setType(issueTypeId);
                    ri.setSummary(text);
                    ri.setDescription(String.format("{%s,%s,%s}", x, y, z));

                    log.info("creating...");
                    RemoteIssue issue = service.createIssue(token, ri);
                    log.info("Created JIRA Issue " + issue.getKey());
                    issueKey = issue.getKey();
                    issues.put(issueKey, new Location(plugin.getServer().getWorld("world"), x, y, z)); // TODO: Won't work in nether?
                }
                catch (Exception e)
                {
                    log.info(e.getMessage());
                    log.info("Uh-oh! " + e.toString());
                    return;
                }

                // set issue key.
                log.info("Scheduling sync delayed task to marshal back to main thread.");
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.getServer().broadcastMessage("Issue " + issueKey + " has been created!");

                        log.info("Sync task now running.");
                        World theWorld = plugin.getServer().getWorld("world"); // TODO: Match world ID to original block's world, otherwise this probably won't work in things like the Nether
                        Block blockAt = theWorld.getBlockAt(x, y, z);
                        log.info("The block in world " + theWorld.getName() + " at position " + x + "," + y + "," + z + " is " + blockAt.getType().toString());
                        if (blockAt.getType().equals(Material.SIGN_POST))
                        {
                            log.info("Preparing to update sign.");
                            Sign signage = (Sign) blockAt.getState();
                            String lineOrig = signage.getLine(0);
                            lineOrig = lineOrig.replace("{jira}", "{" + issueKey + "}");
                            log.info("New first line text: " + lineOrig);
                            signage.setLine(0, lineOrig);
                            signage.update();
                        }
                        log.info("sync task complete");
                    }
                });
                log.info("Async task complete");
            }
        });
    }
}
