package au.id.jaysee.minecraft;

import au.id.jaysee.helpers.Pair;
import au.id.jaysee.minecraft.jira.client.JiraClient;
import org.bukkit.Material;
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
    private final JavaPlugin parentPlugin;
    private final JiraClient jiraClient;
    private final Logger log;

    public McJiraBlockListener(final JavaPlugin parentPlugin, final JiraClient jiraClient, final Logger log)
    {
        this.parentPlugin = parentPlugin;
        this.jiraClient = jiraClient;
        this.log = log;
    }

    private static final String JIRA_SIGN_KEY = "{jira}";
    private static final String JIRA_ISSUE_KEY_REGEX = "\\{[A-Z]+-[0-9]+}"; // TODO: Ensure this is accurate.

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
        String issueSummary = getJiraIssueSummary(event);
        Block signBlock = event.getBlock();
        jiraClient.createIssue(event.getPlayer().getDisplayName(), issueSummary, signBlock.getX(), signBlock.getY(), signBlock.getZ());
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
        String issueKey = matchData.getRight();
        log.info(String.format("Sign for issueKey %s was destroyed; issue should be resolved.", issueKey));

        executor.executeAsync(new AsyncTask<String>() {
            public String doIt()
            {
                jiraClient.resolveIssue(issueKey, event.getPlayer().getDisplayName());
                return issueKey;
            }
        }, new AsyncCallback<String>() {
            public void actionResult(String input)
            {
                parentPlugin.getServer().broadcast("Resolved issue " + input);
            }
        });

        jiraClient.resolveIssue(issueKey, event.getPlayer().getDisplayName());
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
