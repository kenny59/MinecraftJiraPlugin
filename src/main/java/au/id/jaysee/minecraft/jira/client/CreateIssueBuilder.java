package au.id.jaysee.minecraft.jira.client;

import org.json.simple.JSONObject;

/**
 *
 */
final class CreateIssueBuilder
{
    /**
     * Example JSON for creating issue
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
     */

    private CreateIssueBuilder()
    {

    }

    private String projectKey;
    private String reporter;
    private String summary;
    private String issueType;

    public CreateIssueBuilder setProject(String projectKey)
    {
        this.projectKey = projectKey;
        return this;
    }

    public CreateIssueBuilder setReporter(String username)
    {
        this.reporter = username;
        return this;
    }

    public CreateIssueBuilder setSummary(String summary)
    {
        this.summary = summary;
        return this;
    }

    public CreateIssueBuilder setIssueType(String name)
    {
        this.issueType = name;
        return this;
    }

    public static CreateIssueBuilder get()
    {
        return new CreateIssueBuilder();
    }


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
