package au.id.jaysee.minecraft.jira.client;

/**
 *
 */
public class JiraIssue
{
    private String key;
    private String summary;

    public JiraIssue()
    {

    }

    public JiraIssue(String key)
    {
        this.key = key;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
