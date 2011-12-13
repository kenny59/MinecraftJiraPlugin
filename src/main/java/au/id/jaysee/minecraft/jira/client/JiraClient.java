package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

import java.util.Collection;

/**
 *
 */
public interface JiraClient {

    public void resolveIssue(String issueKey, String user);

    public Location getIssueLocation(String issueKey);
    public JiraIssues getIssues();


    // TODO: a more fluent interface for creating issues.
    public JiraIssue createIssue(final String creator, final String text, final int x, final int y, final int z);

}
