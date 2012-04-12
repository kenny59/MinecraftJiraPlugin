package au.id.jaysee.minecraft.jira.client.auth;

import au.id.jaysee.minecraft.config.Configuration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

/**
 *
 */
public class BasicAuthenticatedResourceFactory implements AuthenticatedResourceFactory
{
    private Client jerseyClient;

    private final Configuration config;
    private final Logger log;

    public BasicAuthenticatedResourceFactory(final Configuration pluginConfiguration, final Logger log)
    {
        this.config = pluginConfiguration;
        this.log = log;
        initJerseyConfig();
    }

    /**
     * Performs global configuration of the Jersey client.
     */
    private void initJerseyConfig()
    {
        ClientConfig clientConfig = new DefaultClientConfig();
        // Allow Jersey to map from Java Objects to JSON.
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        jerseyClient = Client.create(clientConfig);

        // Spam the minecraft console with all the HTTP traffic only if debug logging has been enabled.
        if (config.isDebugLoggingEnabled())
            jerseyClient.addFilter(new LoggingFilter(log));

        jerseyClient.addFilter(new HTTPBasicAuthFilter(config.getJiraAdminUsername(), config.getJiraAdminPassword()));
    }


    @Override
    public boolean login()
    {
        return true; // Nothing to do here - auth header handled by filter created in initJerseyConfig.
    }

    @Override
    public WebResource.Builder getResource(String resourceRelativeURL) {

        WebResource resource = jerseyClient.resource(config.getJiraBaseUrl() + resourceRelativeURL);
        WebResource.Builder builder = resource.getRequestBuilder();

        // HACK: All the resources we're going to communicate with send and receive application/json.
        // This isn't really the place to set this, but it's convenient.
        builder = builder.type(MediaType.APPLICATION_JSON);
        builder = builder.accept(MediaType.APPLICATION_JSON);

        return builder;
    }
}
