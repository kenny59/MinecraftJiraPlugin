package au.id.jaysee.minecraft.jira.client.auth.resource;

/**
 * Represents the resource that may be POSTed to the /rest/auth/1/session resource in the JIRA REST API. The object is
 * serialized to JSON before being transmitted.
 *
 * @author Joe Clark
 * @see <a href="http://docs.atlassian.com/jira/REST/5.0-rc2/#id3420885>JIRA 5.0-rc2 REST API documentation: /rest/auth/1/session</a>
 * @since 1.0
 */
public class UsernameAndPassword
{
    private final String username;
    private final String password;

    /**
     * Constructs the resource with the specified username and password.
     *
     * @param username The user to be authenticated
     * @param password The user's password.
     */
    public UsernameAndPassword(final String username, final String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * @return Returns the username of the authenticating user.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return Returns the authenticating user's password.
     */
    public String getPassword()
    {
        return password;
    }

}
