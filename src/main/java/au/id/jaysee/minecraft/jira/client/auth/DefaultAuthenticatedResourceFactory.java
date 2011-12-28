package au.id.jaysee.minecraft.jira.client.auth;

import au.id.jaysee.minecraft.config.Configuration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class DefaultAuthenticatedResourceFactory implements AuthenticatedResourceFactory
{
    private final Configuration pluginConfiguration;
    private final Logger log;
    private Client jerseyClient;
    private static final String AUTH_RELATIVE_URL_FORMAT = "%s/rest/auth/1/session";

    public DefaultAuthenticatedResourceFactory(final Configuration pluginConfiguration, final Logger log)
    {
        this.pluginConfiguration = pluginConfiguration;
        this.log = log;
        initJerseyConfig();
    }

    private void initJerseyConfig()
    {
        ClientConfig clientConfig = new DefaultClientConfig();
        // Allow Jersey to map from Java Objects to JSON.
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        jerseyClient = Client.create(clientConfig);
        if (pluginConfiguration.isDebugLoggingEnabled())
            jerseyClient.addFilter(new LoggingFilter(log));
    }

    private String getAuthURL()
    {
        return String.format(AUTH_RELATIVE_URL_FORMAT, pluginConfiguration.getJiraBaseUrl());
    }

    List<NewCookie> cookies;
    private boolean loggedIn = false;

    public boolean login()
    {
        WebResource authResource = jerseyClient.resource(getAuthURL());
        WebResource.Builder builder = authResource.accept(MediaType.APPLICATION_JSON);
        builder.type(MediaType.APPLICATION_JSON);

        ClientResponse response = builder.post(ClientResponse.class, new UsernameAndPassword(pluginConfiguration.getJiraAdminUsername(), pluginConfiguration.getJiraAdminPassword()));
        if (response.getStatus() != HttpURLConnection.HTTP_OK)
        {
            log.severe(String.format("Login to JIRA Server at %s as user %s failed. Connection returned: %s (%s)", pluginConfiguration.getJiraBaseUrl(), pluginConfiguration.getJiraAdminUsername(), response.getStatus(), response.getClientResponseStatus().getReasonPhrase()));
            return false;
        }

        // Cache these mofos for future use.
        cookies = response.getCookies();

        loggedIn = true; // TODO: Synchronise this.
        return true;
    }

    @Override
    public WebResource.Builder getResource(String resourceRelativeURL)
    {
        WebResource resource = jerseyClient.resource(pluginConfiguration.getJiraBaseUrl() + resourceRelativeURL);
        WebResource.Builder builder = resource.getRequestBuilder();

        if (!loggedIn) // TODO: Synchronise this.
        {
            log.severe("Resource factory not logged in; request will be made anonymously.");
            return builder;
        }

        for (Cookie c : cookies)
        {
            builder = builder.cookie(c);
        }
        return builder;
    }
}
