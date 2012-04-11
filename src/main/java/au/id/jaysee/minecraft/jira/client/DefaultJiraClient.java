package au.id.jaysee.minecraft.jira.client;

import au.id.jaysee.minecraft.jira.client.auth.AuthenticatedResourceFactory;
import au.id.jaysee.minecraft.jira.client.resource.CreateIssueBuilder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
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

    private final Map<String, IssueLocation> issueLocationCache = new HashMap<String, IssueLocation>();

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
        WebResource.Builder builder = authenticatedResourceFactory.getResource("/rest/api/2/issue/" + issueKey);

        ClientResponse getIssueResponse = builder.get(ClientResponse.class);
        // TODO: assert status = 200

        JSONObject responseObj = getIssueResponse.getEntity(JSONObject.class);
        return JiraIssue.parse(responseObj, locationCustomFieldId);
    }

    @Override
    public boolean resolveIssue(String issueKey, String user)
    {
        ///api/2/issue/{issueIdOrKey}/transitions?transitionId
        WebResource.Builder builder =  authenticatedResourceFactory.getResource(String.format("/rest/api/2/issue/%s/transitions", issueKey));

        final JSONObject transition = TransitionBuilder.get().setResolution("Fixed").setTransition(5).addComment(String.format("Resolved by Minecraft User %s", user)).build();
        ClientResponse searchResponse = builder.post(ClientResponse.class, transition);
        return searchResponse.getStatus() == 204;
    }

    @Override
    public IssueLocation getIssueLocation(String issueKey)
    {
        if (issueLocationCache.containsKey(issueKey))
            return issueLocationCache.get(issueKey);

        return getIssue(issueKey).getLocation();
    }

    @Override
    public JiraIssues getIssues()
    {
        log.info("** getting JIRA issues");
        UriBuilder uriBuilder = UriBuilder.fromPath("/rest/api/2/search").queryParam("maxResults", "10").queryParam("jql", String.format("project = %s & resolution = unresolved", minecraftProjectKey));
        URI uri = uriBuilder.build();

        WebResource.Builder builder = authenticatedResourceFactory.getResource(uri.toString());
        ClientResponse searchResponse = builder.get(ClientResponse.class);
        JSONObject entity = searchResponse.getEntity(JSONObject.class);

        return JiraIssues.parse(entity, locationCustomFieldId);
    }

    @Override
    public JiraIssue createIssue(String creator, String text, String world, int x, int y, int z)
    {
        WebResource.Builder builder = authenticatedResourceFactory.getResource("/rest/api/2/issue");

        JSONObject requestObject = CreateIssueBuilder.get().setProject(minecraftProjectKey).setReporter(adminUsername).setSummary(text).setIssueType("Bug").build();

        ClientResponse createResponse = builder.post(ClientResponse.class, requestObject);
        // TODO: assert response status = 200
        String issueKey = createResponse.getEntity(JSONObject.class).get("key").toString();

        // update the location field.
        WebResource.Builder builder2  = authenticatedResourceFactory.getResource("/rest/api/2/issue/" + issueKey);

        final String coordinates = String.format("{world:%s,x:%s,y:%s,z:%s}", world, x, y, z);
        final String customFieldName = "customfield_" + locationCustomFieldId;
        JSONObject updateObject = UpdateIssueBuilder.get().setField(customFieldName, coordinates).build();

        ClientResponse updateResponse = builder2.put(ClientResponse.class, updateObject);
        // TODO: assert response
        JiraIssue result = new JiraIssue(issueKey);

        // Cache the location of the sign.
        issueLocationCache.put(result.getKey(), new IssueLocation(world, x, y, z));
        return result;
    }
}
