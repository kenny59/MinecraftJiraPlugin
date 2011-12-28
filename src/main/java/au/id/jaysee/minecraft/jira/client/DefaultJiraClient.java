package au.id.jaysee.minecraft.jira.client;

import au.id.jaysee.minecraft.jira.client.auth.AuthenticatedResourceFactory;
import au.id.jaysee.minecraft.jira.client.auth.AuthenticationException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class DefaultJiraClient implements JiraClient
{
    private final Logger log = Logger.getLogger("Minecraft");

    private final AuthenticatedResourceFactory authenticatedResourceFactory;

    private final String minecraftProjectKey;
    private final String adminUsername;
    private final String locationCustomFieldId;

    private final Map<String, CacheableLocation> issueLocationCache = new HashMap<String, CacheableLocation>();

    public final class CacheableLocation
    {
        private final int x;
        private final int y;
        private final int z;

        CacheableLocation(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        CacheableLocation(Location l)
        {
            this.x = l.getBlockX();
            this.y = l.getBlockY();
            this.z = l.getBlockZ();
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public int getZ()
        {
            return z;
        }
    }

    public DefaultJiraClient(final AuthenticatedResourceFactory authenticatedResourceFactory, final String locationCustomFieldId, final String minecraftProjectKey, final String adminUsername)
    {
        this.authenticatedResourceFactory = authenticatedResourceFactory;

        this.minecraftProjectKey = minecraftProjectKey;
        this.adminUsername = adminUsername;
        this.locationCustomFieldId = locationCustomFieldId;
    }

    @Override
    public JiraIssue getIssue(String issueKey)
    {
        WebResource.Builder builder = null;
        try
        {
            builder = authenticatedResourceFactory.getResource("/rest/api/2/issue/" + issueKey);
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        builder = builder.type("application/json");
        builder = builder.accept("application/json");

        ClientResponse getIssueResponse = builder.get(ClientResponse.class);
        // TODO: assert status = 200

        JSONObject responseObj = getIssueResponse.getEntity(JSONObject.class);
        return JiraIssue.parse(responseObj, locationCustomFieldId);
    }

    @Override
    public boolean resolveIssue(String issueKey, String user)
    {
        ///api/2/issue/{issueIdOrKey}/transitions?transitionId
        WebResource.Builder builder = null;
        try
        {
            builder = authenticatedResourceFactory.getResource(String.format("/rest/api/2/issue/%s/transitions", issueKey));
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        builder = builder.type("application/json");
        builder = builder.accept("application/json");

        /**
         * {
         *      "update": {
         *          "comment" : {
         *              "add" : {
         *                  "body" : "Issue resolved by %s"
         *              }
         *          }
         *      },
         *      "fields": {
         *          "resolution": {
         *              "name" : "Fixed"
         *          }
         *      },
         *      "transition" : {
         *          "name" : "Resolve Issue"
         *      }
         * }
         */

        JSONObject transitionCommand = new JSONObject();
        JSONObject update = new JSONObject();
        transitionCommand.put("update", update);

        // comment: [ ]
        JSONArray comment = new JSONArray();
        update.put("comment", comment);

        JSONObject addCommentObject = new JSONObject();

        JSONObject add = new JSONObject();
        add.put("body", String.format("Resolved by Minecraft User %s", user));
        addCommentObject.put("add", add);

        comment.add(addCommentObject);

        JSONObject fields = new JSONObject();
        transitionCommand.put("fields", fields);
        JSONObject resolution = new JSONObject();
        resolution.put("name", "Fixed");
        fields.put("resolution", resolution);

        JSONObject transition = new JSONObject();
        transition.put("id", 5); // Transition with ID5 = "Resolve Issue" in a default JIRA project.
        transitionCommand.put("transition", transition);

        ClientResponse searchResponse = builder.post(ClientResponse.class, transitionCommand);
        return searchResponse.getStatus() == 204;
    }

    @Override
    public CacheableLocation getIssueLocation(String issueKey)
    {
        if (issueLocationCache.containsKey(issueKey))
            return issueLocationCache.get(issueKey);

        JiraIssue.IssueLocation location = getIssue(issueKey).getLocation();
        return new CacheableLocation(location.x, location.y, location.z);
    }

    @Override
    public JiraIssues getIssues()
    {
        log.info("** getting JIRA issues");
        UriBuilder uriBuilder = UriBuilder.fromPath("/rest/api/2/search").queryParam("maxResults", "10").queryParam("jql", String.format("project = %s & resolution = unresolved", minecraftProjectKey));
        URI uri = uriBuilder.build();


        WebResource.Builder builder = null;
        try
        {
            builder = authenticatedResourceFactory.getResource(uri.toString());
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        builder = builder.type("application/json");
        builder = builder.accept("application/json");

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
        ClientResponse searchResponse = builder.get(ClientResponse.class);
        JSONObject entity = searchResponse.getEntity(JSONObject.class);

        return JiraIssues.parse(entity, locationCustomFieldId);
    }

    @Override
    public JiraIssue createIssue(String creator, String text, int x, int y, int z)
    {
        /** Example JSON for creating issue
         {
         "fields" : {
         "project" : {
         "key" : "MC"
         },
         "reporter": {
         "name" : "admin"
         },
         "summary": "this is a test",
         "issuetype" : {
         "name" : "Bug"
         }
         }
         }
         **/


        WebResource.Builder builder = null;
        try
        {
            builder = authenticatedResourceFactory.getResource("/rest/api/2/issue");
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        builder = builder.type("application/json");
        builder = builder.accept("application/json");


        JSONObject jiraIssue = new JSONObject();
        JSONObject fields = new JSONObject();
        jiraIssue.put("fields", fields);

        JSONObject project = new JSONObject();
        project.put("key", this.minecraftProjectKey);
        fields.put("project", project);

        JSONObject reporter = new JSONObject();
        reporter.put("name", adminUsername);
        fields.put("reporter", reporter);


        fields.put("summary", text);

        JSONObject issueType = new JSONObject();
        issueType.put("name", "Bug");
        fields.put("issuetype", issueType);

        ClientResponse createResponse = builder.post(ClientResponse.class, jiraIssue);
        // TODO: assert response status = 200
        String issueKey = createResponse.getEntity(JSONObject.class).get("key").toString();

        // update the location field.
        WebResource.Builder builder2 = null;
        try
        {
            builder2 = authenticatedResourceFactory.getResource("/rest/api/2/issue/" + issueKey);
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        builder2 = builder2.type("application/json");
        builder2 = builder2.accept("application/json");

        JSONObject updateIssue = new JSONObject();
        JSONObject update = new JSONObject();
        updateIssue.put("update", update);

        JSONArray customField = new JSONArray();
        JSONObject setCommand = new JSONObject();
        setCommand.put("set", String.format("{world:%s,x:%s,y:%s,z:%s}", "world", x, y, z));
        customField.add(setCommand);
        update.put("customfield_" + locationCustomFieldId, customField);

        ClientResponse updateResponse = builder2.put(ClientResponse.class, updateIssue);

        JiraIssue result = new JiraIssue(issueKey);

        // Cache the location of the sign.
        issueLocationCache.put(result.getKey(), new CacheableLocation(x, y, z));
        return result;
    }
}
