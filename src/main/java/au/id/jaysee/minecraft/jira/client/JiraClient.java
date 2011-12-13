package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

import java.util.Collection;

/**
 *
 */
public interface JiraClient {

    // TODO: Return the error message from the server, rather than just a simple success/fail flag
    public boolean resolveIssue(String issueKey, String user);

    public DefaultJiraClient.CacheableLocation getIssueLocation(String issueKey);
    public JiraIssues getIssues();

    JiraIssue getIssue(String issueKey);



    // TODO: a more fluent interface for creating issues.
    public JiraIssue createIssue(final String creator, final String text, final int x, final int y, final int z);

}
