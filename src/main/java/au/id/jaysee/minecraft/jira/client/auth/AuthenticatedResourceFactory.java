package au.id.jaysee.minecraft.jira.client.auth;

import com.sun.jersey.api.client.WebResource;

/**
 * Defines a factory component that can be used by {@link au.id.jaysee.minecraft.jira.client.JiraClient} instances to
 * create authenticated web requests to JIRA. The authentication method that is chosen may vary depending on the
 * implementation. The {@link DefaultAuthenticatedResourceFactory} uses the "session" resource in the JIRA 5.0 REST API
 * to acquire a re-usable session to the JIRA server.
 *
 * @author Joe Clark
 * @see <a href="http://docs.atlassian.com/jira/REST/5.0-rc2/#id3420885>JIRA 5.0-rc2 REST API documentation: /rest/auth/1/session</a>
 * @since 1.0
 */
public interface AuthenticatedResourceFactory
{
    /**
     * <p/>
     * Instructs the factory to perform an initial authentication against JIRA. If the factory implementation supports
     * re-usable authentication (ie. an authentication "session"), then the login to JIRA will occur once during the
     * invocation of this method. Subsequent calls to {@link #getResource(String)} will re-use the existing login
     * session.
     * <p/>
     * If the authentication method requires that each request is authenticated individually, then this method serves a
     * purely diagnostic/testing purpose.
     *
     * @return Returns {@code true} if the login was successful; {@code false} otherwise.
     */
    public boolean login();

    /**
     * Returns a Jersey client builder object for communicating with the resource identified by the specified relative
     * URL. The builder is pre-primed with the necessary information to authenticate successfully against the JIRA REST
     * API.
     *
     * @param resourceRelativeURL The URL to the REST Resource, relative to the JIRA Base URL specified in the
     *                            {@link au.id.jaysee.minecraft.config.Configuration} object.
     * @return Returns a {@link WebResource.Builder} for the specified URL.
     */
    public WebResource.Builder getResource(String resourceRelativeURL);


}
