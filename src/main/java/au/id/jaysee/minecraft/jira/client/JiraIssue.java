package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

/**
 *
 */
public class JiraIssue
{
    private String key;

    public JiraIssue()
    {

    }

    public JiraIssue(String key)
    {
        this.key = key;
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
