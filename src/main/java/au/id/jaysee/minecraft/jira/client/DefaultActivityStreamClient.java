package au.id.jaysee.minecraft.jira.client;

import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.jira.client.auth.AuthenticatedResourceFactory;
import au.id.jaysee.minecraft.jira.client.resource.ActivityBuilder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import java.util.logging.Logger;

/**
 *
 */
public class DefaultActivityStreamClient implements  ActivityStreamClient
{
    private final AuthenticatedResourceFactory authenticatedResourceFactory;
    private final Logger log;
    private final Configuration pluginConfig;
    private long counter = 0;


    public DefaultActivityStreamClient(Plugin plugin, Configuration pluginConfig, AuthenticatedResourceFactory authenticatedResourceFactory)
    {
        this.authenticatedResourceFactory = authenticatedResourceFactory;
        this.pluginConfig = pluginConfig;
        this.log = plugin.getLogger();
    }

    @Override
    public void postActivity(Player actor, String titleHtml, String contentHtml)
    {
        counter++;

        ActivityBuilder builder = ActivityBuilder.get();
        JSONObject activity = builder.setActor(pluginConfig.getJiraAdminUsername()) // TODO: Use the current user's name.
                                     .setTitle(titleHtml)
                                     .setContent(contentHtml)
                                     .setId("http://minecraft.net/" + String.valueOf(counter))
                                     .build();

        // POST to /rest/activities/1.0
        WebResource.Builder activityResource = authenticatedResourceFactory.getResource("/rest/activities/1.0/");
        activityResource.type("application/vnd.atl.streams.thirdparty+json");
        activityResource.entity(activity);

        ClientResponse response = activityResource.post(ClientResponse.class);
        log.info("Posted and returned: " + response.getClientResponseStatus().getReasonPhrase());
    }
}
