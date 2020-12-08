package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.JiraIssuesHelper;
import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.task.TaskExecutor;
import au.id.jaysee.minecraft.task.Callback;
import au.id.jaysee.minecraft.task.Task;
import au.id.jaysee.utils.EnumUtils;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class McJiraBlockListener implements Listener
{
    private static final String JIRA_SIGN_KEY = "{jira}";
    private static final String JIRA_ISSUE_KEY_REGEX = "\\{[A-Z]+-[0-9]+}"; // TODO: Ensure this is accurate.

    private final JavaPlugin parentPlugin;
    private final JiraIssuesHelper jiraClient;
    private final TaskExecutor taskExecutor;
    private final Configuration config;
    private final Logger log;

    public McJiraBlockListener(final JavaPlugin parentPlugin, final JiraIssuesHelper jiraClient, final TaskExecutor taskExecutor, final Logger log, final Configuration config)
    {
        this.parentPlugin = parentPlugin;
        this.jiraClient = jiraClient;
        this.log = log;
        this.taskExecutor = taskExecutor;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(final SignChangeEvent event)
    {

        if (!isNewJiraSign(event)) {
            return;
        }

        // Create a JIRA issue from this sign; strip away the prefix to get the issue text.
        final String issueSummary = getJiraIssueSummary(event);
        final Block signBlock = event.getBlock();
        final Location l = signBlock.getLocation();
        final World w = signBlock.getWorld();
        final String user = event.getPlayer().getDisplayName();

        taskExecutor.executeAsyncTask(new Task<String>()
                {
                    @Override
                    public String execute() {
                        return jiraClient.createIssue(event.getPlayer(), issueSummary, l);
                    }
                }, new Callback<String>()
        {
            @Override
            public void execute(String input)
            {
                if (input != null)
                {
                    parentPlugin.getServer().getPlayer(user).chat("Created new JIRA Issue " + input + ": " + issueSummary);

                    Block blockLatest = w.getBlockAt(l);
                    log.info("The block in world " + w.getName() + " at position " + l.toString() + " is " + blockLatest.getType().toString());
                    if (EnumUtils.isSign(blockLatest.getType().name()))
                    {
                        log.info("Preparing to update sign.");
                        Sign signage = (Sign) blockLatest.getState();
                        String lineOrig = signage.getLine(0);
                        lineOrig = lineOrig.replace("{jira}", "{" + input + "}");
                        log.info("New first line text: " + lineOrig);
                        signage.setLine(0, lineOrig);
                        signage.update();
                        log.info("Sign Updated.");
                    }
                }
                else
                {
                    parentPlugin.getServer().getPlayer(user).sendMessage("Could not create new JIRA Issue :(");
                }
            }
        }
        );
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void colorEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        if(EnumUtils.isDye(event.getPlayer().getInventory().getItemInMainHand().getType().name())) {
            if(block != null &&
                    event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                    EnumUtils.isSign(block.getType().name())) {
                Sign sign = (Sign) block.getState();
                if(event.getPlayer().getInventory().getItemInMainHand().getType().name().equals(Material.RED_DYE.name())) {
                    Issue matchData = isExistingJiraSign(sign);
                    if(matchData != null) {
                        taskExecutor.executeAsyncTask(new Task<Boolean>()
                        {
                            @Override
                            public Boolean execute() throws URISyntaxException {
                                return jiraClient.resolveIssue("TODO", matchData.getKey());
                            }
                        }, new Callback<Boolean>()
                        {
                            @Override
                            public void execute(Boolean input)
                            {
                                if (!input)
                                {
                                    log.warning("Attempt to resolve JIRA Issue " + matchData.getKey() + " did not succeed, but sign is colored.");
                                    return;
                                }

                                event.getPlayer().chat("Resolved JIRA issue to 'In Progress' " + matchData.getKey());
                            }
                        });

                    }
                }
            }

        }
    }

    List<Block> getSignOnBlock(Block block) {
        return List.of(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST)
                .stream().map(blockFace -> {
                    if(EnumUtils.isSign(block.getRelative(blockFace).getType().name())) {
                        return block.getRelative(blockFace);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event)
    {
        Block brokenBlock = event.getBlock();
        if (!List.of(EnumUtils.isSign(brokenBlock.getType().name()), getSignOnBlock(brokenBlock).size()>0).contains(true))
        {
            if (config.isDebugLoggingEnabled())
            {
                log.info("Block was not a sign post, exiting.");
            }
            return;
        }

        if (!List.of((brokenBlock.getState() instanceof Sign), getSignOnBlock(brokenBlock).size()>0).contains(true))
        {
            if (config.isDebugLoggingEnabled())
            {
                log.info("Block did not contain sign state, exiting.");
            }
            return;
        }

        List<Block> blocks = getSignOnBlock(brokenBlock);
        Sign signage = null;

        if(blocks.size()>0) {
            signage = (Sign) blocks.get(0).getState();
        } else {
            signage = (Sign) brokenBlock.getState();
        }

        final Issue matchData = isExistingJiraSign(signage);
        if (matchData == null)
        {
            if (config.isDebugLoggingEnabled())
            {
                log.info("Sign was not a JIRA issue.");
            }
            return;
        }
        // Existing JIRA sign.
        final String issueKey = matchData.getKey();
        final String user = event.getPlayer().getDisplayName();
        log.info(String.format("Sign for issueKey %s was destroyed; issue should be resolved.", issueKey));

        final int x = brokenBlock.getX();
        final int y = brokenBlock.getY();
        final int z = brokenBlock.getZ();
        final String[] originalSignData = signage.getLines();

        // do it.
        taskExecutor.executeAsyncTask(new Task<Boolean>()
        {
            @Override
            public Boolean execute() throws URISyntaxException {
                return jiraClient.resolveIssue("DONE", issueKey);
            }
        }, new Callback<Boolean>()
        {
            @Override
            public void execute(Boolean input)
            {
                if (!input)
                {
                    log.warning("Attempt to resolve JIRA Issue " + issueKey + " did not succeed, but the sign is being destroyed anyway.");
                    return;
                }

                // All good.
                parentPlugin.getServer().getPlayer(user).chat("Resolved JIRA issue " + issueKey);
            }
        }
        );
    }

    private boolean isNewJiraSign(SignChangeEvent event)
    {
        String firstLine = event.getLine(0);
        return firstLine.equalsIgnoreCase(JIRA_SIGN_KEY);
    }

    private Issue isExistingJiraSign(Sign sign)
    {
        Pattern issueKeyPattern = Pattern.compile(JIRA_ISSUE_KEY_REGEX);
        Matcher issueKeyMatcher = issueKeyPattern.matcher(sign.getLine(0));

        if(issueKeyMatcher.matches()) {
            String issueKey = issueKeyMatcher.group().replace("{", "").replace("}", "");
            System.out.println(issueKey);
            return jiraClient.getIssueByKey(issueKey);
        } else {
            return null;
        }
    }

    private String getJiraIssueSummary(SignChangeEvent event)
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 1; i < event.getLines().length; i++)
        {
            builder.append(event.getLine(i));
        }
        return builder.toString();
    }
}
