package au.id.jaysee.minecraft.config;

/**
 * A plain-old Java object that holds the configuration values for the Minecraft JIRA plugin and may be passed around
 * to components in the plugin that need to be aware of the plugin's configuration.
 *
 * @author Joe Clark
 * @since 1.0
 * @see {@link ConfigurationLoader#load}
 */
public final class Configuration
{
    private final String jiraBaseUrl;
    private final String minecraftProjectKey;
    private final String jiraAdminUsername;
    private final String jiraAdminPassword;
    private final String locationCustomFieldId;
    private final boolean debugLoggingEnabled;

    // Feature switches
    private final boolean dynamicUserCreation;

    /**
     * Constructs a new Configuration POJO. This constructor has package-only access; should only be called by
     * {@link ConfigurationLoader#load}.
     *
     * @param jiraBaseUrl           The base URL of the JIRA Server to connect to.
     * @param minecraftProjectKey   The project key of the project to use for creating Minecraft issues.
     * @param jiraAdminUsername     Username of a JIRA user with admin privileges - used for authenticating against the JIRA server.
     * @param jiraAdminPassword     Password of the JIRA user identified by the {@literal jiraAdminUsername} parameter.
     * @param locationCustomFieldId The Field ID of the custom field that is used to persist the world co-ordinates of JIRA issue.
     * @param debugLoggingEnabled   Whether or not diagnostic logging will be printed to the server console.
     */
    Configuration(String jiraBaseUrl, String minecraftProjectKey, String jiraAdminUsername, String jiraAdminPassword, String locationCustomFieldId, boolean debugLoggingEnabled, boolean dynamicUserCreation)
    {
        this.jiraBaseUrl = jiraBaseUrl;
        this.minecraftProjectKey = minecraftProjectKey;
        this.jiraAdminUsername = jiraAdminUsername;
        this.jiraAdminPassword = jiraAdminPassword;
        this.locationCustomFieldId = locationCustomFieldId;
        this.debugLoggingEnabled = debugLoggingEnabled;

        this.dynamicUserCreation = dynamicUserCreation;
    }

    /**
     * Gets the value of the feature switch indicating if the Minecraft plugin should automatically create matching user
     * accounts in JIRA for users logging in to the Minecraft server.
     */
    public boolean isDynamicUserCreationEnabled()
    {
        return dynamicUserCreation;
    }

    /**
     * Gets the base URL of the JIRA Server to connect to (eg. "http://jira.atlassian.com").
     */
    public String getJiraBaseUrl()
    {
        return jiraBaseUrl;
    }

    /**
     * The project key of the project to use for creating Minecraft issues(eg. "MC").
     */
    public String getMinecraftProjectKey()
    {
        return minecraftProjectKey;
    }

    /**
     * Username of a JIRA user with admin privileges - used for authenticating against the JIRA server (eg. "admin").
     */
    public String getJiraAdminUsername()
    {
        return jiraAdminUsername;
    }

    /**
     * Password of the JIRA user identified by the {@literal jiraAdminUsername} parameter (eg. "admin").
     */
    public String getJiraAdminPassword()
    {
        return jiraAdminPassword;
    }

    /**
     * The Field ID of the custom field that is used to persist the world co-ordinates of JIRA issue (eg. "10000").
     */
    public String getLocationCustomFieldId()
    {
        return locationCustomFieldId;
    }

    /**
     * Whether or not diagnostic logging will be printed to the server console.
     */
    public boolean isDebugLoggingEnabled()
    {
        return debugLoggingEnabled;
    }
}
