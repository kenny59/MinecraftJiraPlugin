package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

/**
 *
 */
public class JiraIssue
{
    private final String id;

    public JiraIssue(String id)
    {
        this.id = id;
    }


    public String getId()
    {
        return id;
    }


    public Location getLocation()
    {
        return null;
    }


}
