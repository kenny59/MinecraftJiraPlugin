package au.id.jaysee.minecraft.jira.client.auth;

import au.id.jaysee.minecraft.config.Configuration;
import au.id.jaysee.minecraft.jira.client.auth.resource.UsernameAndPassword;
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
 * Provides a default implementation of the {@link AuthenticatedResourceFactory} that uses the JIRA REST API's "session"
 * resource to negotiate an authenticated session with the JIRA Server.
 *
 * @author Joe Clark
 * @see <a href="http://docs.atlassian.com/jira/REST/5.0-rc2/#id3420885>JIRA 5.0-rc2 REST API documentation: /rest/auth/1/session</a>
 * @since 1.0
 */
class DefaultAuthenticatedResourceFactory implements AuthenticatedResourceFactory
{
    private static final String AUTH_RELATIVE_URL_FORMAT = "%s/rest/auth/1/session";

    private final Configuration pluginConfiguration;
    private final Logger log;

    private Client jerseyClient;
    List<NewCookie> cookies; // Cache the cookies returned from the session resource
    private boolean loggedIn = false;
    private final Object loginLock = new Object();

    /**
     * Constructs the factory.
     *
     * @param pluginConfiguration Minecraft plugin's configuration for connecting to the JIRA server.
     * @param log                 Logger
     */
    public DefaultAuthenticatedResourceFactory(final Configuration pluginConfiguration, final Logger log)
    {
        this.pluginConfiguration = pluginConfiguration;
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
        if (pluginConfiguration.isDebugLoggingEnabled())
            jerseyClient.addFilter(new LoggingFilter(log));
    }

    /**
     * Logs in to the JIRA REST API's "session" resource by transmitting the configured JIRA username and password
     * in the clear as a HTTP POST with a JSON body. The cookies returned from the response (if successful) are cached
     * in memory and copied to all subsequents created through the {@link #getResource(String)} method.
     *
     * @return Returns {@code true} if the login was successful; {@code false} otherwise.
     */
    public boolean login()
    {
        WebResource authResource = jerseyClient.resource(String.format(AUTH_RELATIVE_URL_FORMAT, pluginConfiguration.getJiraBaseUrl()));
        WebResource.Builder builder = authResource.accept(MediaType.APPLICATION_JSON);
        builder = builder.type(MediaType.APPLICATION_JSON);

        ClientResponse response = builder.post(ClientResponse.class, new UsernameAndPassword(pluginConfiguration.getJiraAdminUsername(), pluginConfiguration.getJiraAdminPassword()));
        if (response.getStatus() != HttpURLConnection.HTTP_OK)
        {
            log.severe(String.format("Login to JIRA Server at %s as user %s failed. Connection returned: %s (%s)", pluginConfiguration.getJiraBaseUrl(), pluginConfiguration.getJiraAdminUsername(), response.getStatus(), response.getClientResponseStatus().getReasonPhrase()));
            return false;
        }

        cookies = response.getCookies();
        synchronized (loginLock)
        {
            loggedIn = true;
        }
        return true;
    }

    /**
     * Returns a Jersey client builder object for communicating with the resource identified by the specified relative
     * URL. The builder is pre-primed with the necessary information to authenticate successfully against the JIRA REST
     * API.
     *
     * @param resourceRelativeURL The URL to the REST Resource, relative to the JIRA Base URL specified in the
     *                            {@link au.id.jaysee.minecraft.config.Configuration} object.
     * @return Returns a {@link WebResource.Builder} for the specified URL.
     */
    @Override
    public WebResource.Builder getResource(String resourceRelativeURL)
    {
        WebResource resource = jerseyClient.resource(pluginConfiguration.getJiraBaseUrl() + resourceRelativeURL);
        WebResource.Builder builder = resource.getRequestBuilder();

        final boolean isLoggedIn;
        synchronized (loginLock)
        {
            isLoggedIn = loggedIn;
        }

        if (!isLoggedIn)
        {
            log.severe("Resource factory not logged in; request will be made anonymously.");
            return builder;
        }

        for (Cookie c : cookies)
        {
            builder = builder.cookie(c);
        }

        // HACK: All the resources we're going to communicate with send and receive application/json.
        // This isn't really the place to set this, but it's convenient.
        builder = builder.type(MediaType.APPLICATION_JSON);
        builder = builder.accept(MediaType.APPLICATION_JSON);

        return builder;
    }
}
