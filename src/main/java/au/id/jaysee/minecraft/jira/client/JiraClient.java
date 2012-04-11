package au.id.jaysee.minecraft.jira.client;

import au.id.jaysee.helpers.Either;
import org.bukkit.Location;

import java.util.Collection;

/**
 *
 */
public interface JiraClient {

    // TODO: Return the error message from the server, rather than just a simple success/fail flag
    public boolean resolveIssue(String issueKey, String user);

    public IssueLocation getIssueLocation(String issueKey);
    public JiraIssues getIssues();

    JiraIssue getIssue(String issueKey);



    // TODO: a more fluent interface for creating issues.
    public Either<JiraIssue, JiraError> createIssue(final String creator, final String text, String world, final int x, final int y, final int z);

}
