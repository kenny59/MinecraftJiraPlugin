package au.id.jaysee.minecraft.jira.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class DefaultJiraClient implements JiraClient
{
    private final Logger log = Logger.getLogger("Minecraft");

    private final Plugin minecraftPlugin;
    private final String jiraBaseUrl;
    private final String minecraftProjectKey;
    private final String adminUsername;
    private final String adminPassword;

    public DefaultJiraClient(final Plugin minecraftPlugin, final String jiraBaseUrl, final String minecraftProjectKey, final String adminUsername, final String adminPassword)
    {
        this.minecraftPlugin = minecraftPlugin;
        this.jiraBaseUrl = jiraBaseUrl;
        this.minecraftProjectKey = minecraftProjectKey;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    private static final class UsernameAndPassword
    {
        private final String username;
        private final String password;

        public UsernameAndPassword(final String username, final String password)
        {
            this.username = username;
            this.password = password;
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

    }

    @Override
    public void resolveIssue(String issueKey, String user)
    {
        /** Duplicate Stuff **/
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        client.addFilter(new LoggingFilter(log));

        WebResource authResource = client.resource(jiraBaseUrl + "/rest/auth/1/session");
        WebResource.Builder accept = authResource.accept("application/json");
        accept = accept.type("application/json");

        ClientResponse response = accept.post(ClientResponse.class, new UsernameAndPassword(adminUsername, adminPassword));
        List<NewCookie> authCookies = response.getCookies();
        log.info("Login returned: " + response.getStatus());
        /** **/

        ///api/2/issue/{issueIdOrKey}/transitions?transitionId

        WebResource resolveIssueResource = client.resource(String.format(jiraBaseUrl + "/rest/api/2/issue/%s/transitions", issueKey));
        WebResource.Builder builder = resolveIssueResource.getRequestBuilder();
        for (Cookie c : authCookies)
        {
            builder = builder.cookie(c);
        }
        builder = builder.type("application/json");

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

        JSONObject comment = new JSONObject();
        update.put("comment", comment);
        JSONObject addComment = new JSONObject();
        addComment.put("body", String.format("Resolved by Minecraft User %s", user));
        comment.put("add", addComment);

        JSONObject fields = new JSONObject();
        transitionCommand.put("fields", fields);
        JSONObject resolution = new JSONObject();
        resolution.put("name", "Fixed");
        fields.put("resolution", resolution);

        JSONObject transition = new JSONObject();
        transitionCommand.put("transition", transition);
        JSONObject transitionName = new JSONObject();
        transitionName.put("name", "Resolve Issue");

        ClientResponse searchResponse = builder.post(ClientResponse.class, transitionCommand);
    }

    @Override
    public Location getIssueLocation(String issueKey)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<JiraIssue> getIssues()
    {
        /** Duplicate stuff **/
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        client.addFilter(new LoggingFilter(log));

        WebResource authResource = client.resource(jiraBaseUrl + "/rest/auth/1/session");
        WebResource.Builder accept = authResource.accept("application/json");
        accept = accept.type("application/json");

        ClientResponse response = accept.post(ClientResponse.class, new UsernameAndPassword(adminUsername, adminPassword));
        List<NewCookie> authCookies = response.getCookies();
        log.info("Login returned: " + response.getStatus());
        /** **/

        WebResource searchResource = client.resource(jiraBaseUrl + "/rest/api/2/search");
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("jql", "project = MC");
        queryParams.add("maxResults", "10");
        searchResource = searchResource.queryParams(queryParams);
        WebResource.Builder searchBuilder = searchResource.getRequestBuilder();
        for (Cookie c : authCookies)
        {
            searchBuilder = searchBuilder.cookie(c);
        }

        ClientResponse searchResponse = searchBuilder.get(ClientResponse.class);
        return null; // TODO: Return client object.
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

        /** Duplicate Stuff **/
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        client.addFilter(new LoggingFilter(log));

        WebResource authResource = client.resource(jiraBaseUrl + "/rest/auth/1/session");
        WebResource.Builder accept = authResource.accept("application/json");
        accept = accept.type("application/json");

        ClientResponse response = accept.post(ClientResponse.class, new UsernameAndPassword(adminUsername, adminPassword));
        List<NewCookie> authCookies = response.getCookies();
        log.info("Login returned: " + response.getStatus());
        /** **/


        WebResource createIssueResource = client.resource(jiraBaseUrl + "/rest/api/2/issue");
        WebResource.Builder builder = createIssueResource.getRequestBuilder();
        for (Cookie c : authCookies)
        {
            builder = builder.cookie(c);
        }
        builder = builder.type("application/json");

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

        ClientResponse searchResponse = builder.post(ClientResponse.class, jiraIssue);

        return new JiraIssue(searchResponse.getEntity(JSONObject.class).get("id").toString());

    }
}
