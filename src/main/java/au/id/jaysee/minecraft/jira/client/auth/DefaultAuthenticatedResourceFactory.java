package au.id.jaysee.minecraft.jira.client.auth;

import au.id.jaysee.minecraft.config.Configuration;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;
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
public class DefaultAuthenticatedResourceFactory implements AuthenticatedResourceFactory
{

    private final Configuration pluginConfiguration;
    private final Logger log;


    private JiraRestClient jerseyClient;
    private boolean loggedIn = false;
    private final Object loginLock = new Object();

    /**
     * Constructs the factory.
     *
     * @param pluginConfiguration Minecraft plugin's configuration for connecting to the JIRA server.
     * @param log                 Logger
     */
    public DefaultAuthenticatedResourceFactory(final Configuration pluginConfiguration, final Logger log) throws URISyntaxException {
        this.pluginConfiguration = pluginConfiguration;
        this.log = log;
        initJerseyConfig();
    }

    /**
     * Performs global configuration of the jira rest client.
     * With login: username +  api token
     */
    private void initJerseyConfig() throws URISyntaxException {
        JiraRestClient client = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(new URI(pluginConfiguration.getJiraBaseUrl()), pluginConfiguration.getJiraAdminUsername(), pluginConfiguration.getJiraApiToken());
        jerseyClient = client;
    }

    @Override
    public boolean login() {
        return true;
    }

    public JiraRestClient getClient() {
        return jerseyClient;
    }
}
