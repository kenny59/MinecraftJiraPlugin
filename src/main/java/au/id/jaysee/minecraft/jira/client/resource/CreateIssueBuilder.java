package au.id.jaysee.minecraft.jira.client.resource;

import org.json.simple.JSONObject;

/**
 * <p/>
 * A primitive attempt at a Builder object for the REST Resource that may be POSTed to the JIRA REST API's "issue" resource
 * to create a new JIRA issue. The object is serialized to JSON before being transmitted.
 * <p/>
 * Example JSON representation:
 * <pre>
 *  {
 *      "fields" : {
 *          "project" : {
 *              "key" : "MC"
 *          },
 *          "reporter": {
 *              "name" : "admin"
 *          },
 *          "summary": "this is a test",
 *          "issuetype" : {
 *              "name" : "Bug"
 *          }
 *      }
 * }
 * </pre>
 *
 * @author Joe Clark
 * @see <a href="http://docs.atlassian.com/jira/REST/5.0-rc2/#id3418656">JIRA 5.0-rc2 REST API documentation: /rest/api/2/issue</a>
 *      //
 * @since 1.0
 */
public final class CreateIssueBuilder
{
    private String projectKey;
    private String reporter;
    private String summary;
    private String issueType;

    /**
     * @return Returns a new builder object.
     */
    public static CreateIssueBuilder get()
    {
        return new CreateIssueBuilder();
    }

    /**
     * Only the {@link #get()} method constructs new instances.
     */
    private CreateIssueBuilder()
    {
        // empty
    }

    /**
     * Specifies the unique key of the parent JIRA project for the new issue.
     *
     * @param projectKey The key of the desired project (eg. "JRA")
     * @return Returns the builder back again.
     */
    public CreateIssueBuilder setProject(String projectKey)
    {
        this.projectKey = projectKey;
        return this;
    }

    /**
     * Sets the username of the user who will be marked as the reporter of the new issue.
     *
     * @param username
     * @return Returns the builder back again.
     */
    public CreateIssueBuilder setReporter(String username)
    {
        this.reporter = username;
        return this;
    }

    /**
     * Sets the summary of the new issue.
     *
     * @param summary The desired summary text.
     * @return Returns the builder back again.
     */
    public CreateIssueBuilder setSummary(String summary)
    {
        this.summary = summary;
        return this;
    }

    /**
     * Sets the type of the issue that will be created by the name of the desired issue type.
     *
     * @param name The name of the desired issue type (eg. "Bug").
     * @return Returns the builder back again.
     */
    public CreateIssueBuilder setIssueType(String name)
    {
        this.issueType = name;
        return this;
    }

    /**
     * Serializes the builder to an appropriate JSON representation for sending to JIRA.
     * @return The JSON representation of this builder object.
     */
    @SuppressWarnings("unchecked")
    public JSONObject build()
    {
        JSONObject jiraIssue = new JSONObject();
        JSONObject fields = new JSONObject();
        jiraIssue.put("fields", fields);

        JSONObject project = new JSONObject();
        project.put("key", projectKey);
        fields.put("project", project);

        JSONObject reporter = new JSONObject();
        reporter.put("name", this.reporter);
        fields.put("reporter", reporter);


        fields.put("summary", summary);

        JSONObject issueType = new JSONObject();
        issueType.put("name", this.issueType);
        fields.put("issuetype", issueType);

        return jiraIssue;
    }
}
