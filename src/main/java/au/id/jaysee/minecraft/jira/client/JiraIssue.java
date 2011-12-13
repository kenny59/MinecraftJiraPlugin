package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class JiraIssue
{
    public static class IssueLocation
    {
        public String world;
        public int x;
        public int y;
        public int z;
    }

    private String key;
    private String summary;
    private IssueLocation location;

    public IssueLocation getLocation()
    {
        return location;
    }

    public void setLocation(IssueLocation location)
    {
        this.location = location;
    }


    public JiraIssue()
    {

    }

    private static final String LOCATION_REGEX = "\\{world:(.*),x:(.*),y:(.*),z:(.*)}";

    static JiraIssue parse(Map<String, Object> issueMap, String locationCustomFieldId)
    {
        String key = issueMap.get("key").toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) issueMap.get("fields");

        String summary = fields.get("summary").toString();

        String location = (String) fields.get("customfield_" + locationCustomFieldId);
        IssueLocation l = null;
        if (location != null)
        {

            Pattern locationPattern = Pattern.compile(LOCATION_REGEX);
            Matcher locationMatcher = locationPattern.matcher(location);


            if (locationMatcher.matches())
            {
                l = new IssueLocation();
                l.world = locationMatcher.group(1);
                l.x = new Integer(locationMatcher.group(2));
                l.y = new Integer(locationMatcher.group(3));
                l.z = new Integer(locationMatcher.group(4));
            }
        }

        JiraIssue jiraIssue = new JiraIssue(key);
        jiraIssue.setSummary(summary);
        jiraIssue.setLocation(l);
        return jiraIssue;
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
