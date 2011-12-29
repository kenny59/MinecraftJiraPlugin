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

    static JiraIssues parse(JSONObject entity, String locationCustomFieldId)
    {
               /**
         * Example response JSON:
         *
         * {
         *  "expand": "schema,names",
         *  "startAt":0,
         *  "maxResults":10,
         *  "total":3,
         *  "issues":[
         *      {
         *          "expand":"editmeta,renderedFields,transitions,changelog",
         *          "id":"10002",
         *          "self":"http://localhost:8080/rest/api/2/issue/10002",
         *          "key":"MC-3",
         *          "fields":
         *              {
         *                  "summary":"Hello World",
         *                  "progress":{"progress":0,"total":0},
         *                  "issuetype":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/issuetype/1",
         *                          "id":"1",
         *                          "description":"A problem which impairs or prevents the functions of the product.",
         *                          "iconUrl":"http://localhost:8080/images/icons/bug.gif",
         *                          "name":"Bug",
         *                          "subtask":false
         *                      },
         *                  "votes":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/issue/MC-3/votes",
         *                          "votes": 0,
         *                          "hasVoted":false
         *                      },
         *                  "resolution":null,
         *                  "fixVersions":[],
         *                  "resolutiondate":null,
         *                  "timespent":null,
         *                  "reporter":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/user?username=admin",
         *                          "name":"admin",
         *                          "emailAddress":"jclark@atlassian.com",
         *                          "avatarUrls":
         *                              {
         *                                  "16x16":"http://localhost:8080/secure/useravatar?size=small&avatarId=10122",
         *                                  "48x48":"http://localhost:8080/secure/useravatar?avatarId=10122"
         *                              },
         *                          "displayName":"Washington Irving",
         *                          "active":true
         *                      },
         *                  "aggregatetimeoriginalestimate":null,
         *                  "updated":"2011-12-13T10:56:57.000+1100",
         *                  "created":"2011-12-13T10:56:57.000+1100",
         *                  "description":null,
         *                  "priority":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/priority/3",
         *                          "iconUrl":"http://localhost:8080/images/icons/priority_major.gif",
         *                          "name":"Major",
         *                          "id":"3"
         *                      },
         *                  "duedate":null,
         *                  "issuelinks":[],
         *                  "watches":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/issue/MC-3/watchers",
         *                          "watchCount":0,
         *                          "isWatching":false
         *                      },
         *                  "subtasks":[],
         *                  "status":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/status/1",
         *                          "description":"The issue is open and ready for the assignee to start work on it.",
         *                          "iconUrl":"http://localhost:8080/images/icons/status_open.gif",
         *                          "name":"Open",
         *                          "id":"1"
         *                      },
         *                  "labels":[],
         *                  "assignee":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/user?username=admin",
         *                          "name":"admin",
         *                          "emailAddress":"jclark@atlassian.com",
         *                          "avatarUrls":
         *                              {
         *                                  "16x16":"http://localhost:8080/secure/useravatar?size=small&avatarId=10122",
         *                                  "48x48":"http://localhost:8080/secure/useravatar?avatarId=10122"
         *                              },
         *                          "displayName":"Washington Irving",
         *                          "active":true
         *                      },
         *                  "workratio":-1,
         *                  "aggregatetimeestimate":null,
         *                  "project":
         *                      {
         *                          "self":"http://localhost:8080/rest/api/2/project/MC",
         *                          "id":"10000",
         *                          "key":"MC",
         *                          "name":"Minecraft Tasks",
         *                          "avatarUrls":
         *                              {
         *                                  "16x16":"http://localhost:8080/secure/projectavatar?size=small&pid=10000&avatarId=10011",
         *                                  "48x48":"http://localhost:8080/secure/projectavatar?pid=10000&avatarId=10011"
         *                              }
         *                      },
         *                  "versions":[],
         *                  "environment":null,
         *                  "timeestimate":null,
         *                  "aggregateprogress":
         *                      {
         *                          "progress":0,
         *                          "total":0
         *                      },
         *                  "components":[],
         *                  "timeoriginalestimate":null,
         *                  "aggregatetimespent":null
         *              }
         *      }
         *  ]
         * }
         */

        final int count = (Integer)entity.get("total"); // TODO: possible to avoid boxing/un-boxing?

        Collection<JiraIssue> clientIssues = new LinkedList<JiraIssue>();
        for (Object o : (Iterable)entity.get("issues"))
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> issueMap = (Map<String, Object>)o;
            JiraIssue clientIssue = JiraIssue.parse(issueMap, locationCustomFieldId);
            clientIssues.add(clientIssue);
        }

        return new JiraIssues(clientIssues, count);
    }

}
