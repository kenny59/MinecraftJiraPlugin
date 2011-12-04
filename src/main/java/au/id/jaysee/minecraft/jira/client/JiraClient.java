package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

/**
 *
 */
public interface JiraClient {

    public void resolveIssue(String issueKey, String user);

    //public Collection<JiraIssue> getIssues(String jql);

    public Location getIssueLocation(String issueKey);
    public void printAllIssues();


    // TODO: a more fluent interface for creating issues.
    public JiraIssue createIssue(final String creator, final String text, final int x, final int y, final int z);

}
