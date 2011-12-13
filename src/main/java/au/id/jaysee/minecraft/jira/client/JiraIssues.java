package au.id.jaysee.minecraft.jira.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public class JiraIssues
{
    private Collection<JiraIssue> issues;
    private int count;

    JiraIssues(Collection<JiraIssue> issues, int count)
    {
        this.issues = issues;
        this.count = count;
    }

    public int getCount()
    {
        return count;
    }

    // TODO: Defensive copy or return read-only implementation
    public Collection<JiraIssue> getIssues()
    {
        return issues;
    }

    // TODO: not public.
    public void setIssues(Collection<JiraIssue> issues)
    {

        this.issues = issues;
    }

    static JiraIssues parse(JSONObject entity)
    {
        final int count = (Integer)entity.get("total"); // TODO: possible to avoid boxing/un-boxing?

        Collection<JiraIssue> clientIssues = new LinkedList<JiraIssue>();
        for (Object o : (Iterable)entity.get("issues"))
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> issue = (Map<String, Object>)o;

            JiraIssue clientIssue = new JiraIssue(issue.get("key").toString());

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>)issue.get("fields");

            String summary = fields.get("summary").toString();
            clientIssue.setSummary(summary);

            clientIssues.add(clientIssue);
        }

        return new JiraIssues(clientIssues, count);
    }

}
