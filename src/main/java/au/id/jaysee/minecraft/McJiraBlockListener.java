package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.Pair;
import au.id.jaysee.minecraft.async.AsyncExecutor;
import au.id.jaysee.minecraft.async.Callback;
import au.id.jaysee.minecraft.async.Task;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import au.id.jaysee.minecraft.jira.client.JiraIssue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class McJiraBlockListener extends BlockListener
{
    private static final String JIRA_SIGN_KEY = "{jira}";
    private static final String JIRA_ISSUE_KEY_REGEX = "\\{[A-Z]+-[0-9]+}"; // TODO: Ensure this is accurate.

    private final JavaPlugin parentPlugin;
    private final JiraClient jiraClient;
    private final AsyncExecutor taskExecutor;
    private final Logger log;

    public McJiraBlockListener(final JavaPlugin parentPlugin, final JiraClient jiraClient, final AsyncExecutor taskExecutor, final Logger log)
    {
        this.parentPlugin = parentPlugin;
        this.jiraClient = jiraClient;
        this.log = log;
        this.taskExecutor = taskExecutor;
    }

    /**
     * When the text of a sign is changed, create or update a corresponding JIRA issue.
     */
    @Override
    public void onSignChange(SignChangeEvent event)
    {
        super.onSignChange(event);

        if (!isNewJiraSign(event))
            return;

        // Create a JIRA issue from this sign; strip away the prefix to get the issue text.
        final String issueSummary = getJiraIssueSummary(event);
        final Block signBlock = event.getBlock();
        final Location l = signBlock.getLocation();
        final String user = event.getPlayer().getDisplayName();

        taskExecutor.executeAsyncTask(new Task<String>()
                {
                    @Override
                    public String execute()
                    {
                        final JiraIssue newIssue = jiraClient.createIssue(user, issueSummary, l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        return newIssue.getId();
                    }
                }, new Callback<String>()
        {
            @Override
            public void execute(String input)
            {
                parentPlugin.getServer().getPlayer(user).chat("Created new JIRA Issue " + input);

                // TODO: Retrieve the world with the ID that matches the original block's world; otherwise this probably won't work in things like the Nether and The End.
                World world = parentPlugin.getServer().getWorld("world");
                Block blockLatest = world.getBlockAt(l);
                log.info("The block in world " + world.getName() + " at position " + l.toString() + " is " + blockLatest.getType().toString());
                if (blockLatest.getType().equals(Material.SIGN_POST))
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
        }
        );
    }

    /**
     * When a sign is destroyed, resolve the corresponding JIRA issue, if it exists.
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        log.info("Entering McJiraBlockListener.onBlockBreak");

        super.onBlockBreak(event);
        Block brokenBlock = event.getBlock();
        if (!brokenBlock.getType().equals(Material.SIGN_POST))
        {
            log.info("Block was not a sign post, exiting.");
            return;
        }

        if (!(brokenBlock.getState() instanceof Sign))
        {
            log.info("Block did not contain sign state, exiting.");
            return;
        }

        Sign signage = (Sign) brokenBlock.getState();
        final Pair<Boolean, String> matchData = isExistingJiraSign(signage);
        if (!matchData.getLeft())
        {
            log.info("Sign was not a JIRA issue.");
            return;
        }
        // Existing JIRA sign.
        final String issueKey = matchData.getRight();
        final String user = event.getPlayer().getDisplayName();
        log.info(String.format("Sign for issueKey %s was destroyed; issue should be resolved.", issueKey));

        // do it.
        taskExecutor.executeAsyncTask(new Task<String>()
                {
                    @Override
                    public String execute()
                    {
                        jiraClient.resolveIssue(issueKey, user);
                        return issueKey;
                    }
                }, new Callback<String>()
        {

            @Override
            public void execute(String input)
            {
                parentPlugin.getServer().getPlayer(user).chat("Resolved JIRA issue " + input);
            }
        }
        );
    }

    private boolean isNewJiraSign(SignChangeEvent event)
    {
        String firstLine = event.getLine(0);
        return firstLine.equalsIgnoreCase(JIRA_SIGN_KEY);
    }

    private Pair<Boolean, String> isExistingJiraSign(Sign sign)
    {
        Pattern issueKeyPattern = Pattern.compile(JIRA_ISSUE_KEY_REGEX);
        Matcher issueKeyMatcher = issueKeyPattern.matcher(sign.getLine(0));

        return new Pair<Boolean, String>(issueKeyMatcher.matches(),
                issueKeyMatcher.matches() ?
                        issueKeyMatcher.group().replace("{", "").replace("}", "") :
                        "");
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
