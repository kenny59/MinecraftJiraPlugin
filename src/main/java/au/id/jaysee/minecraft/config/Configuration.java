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
    private final String projectKey;
    private final String parentKey;
    private final String jiraAdminUsername;
    private final String jiraApiToken;
    private final Integer jiraProgressTransition;
    private final Integer jiraDoneTransition;
    private final boolean debugLoggingEnabled;

    private final boolean dynamicUserCreation;

    public Configuration(String jiraBaseUrl, String projectKey, String parentKey, String jiraAdminUsername, String jiraApiToken, String jiraProgressTransition, String jiraDoneTransition, boolean debugLoggingEnabled, boolean dynamicUserCreation)
    {
        this.jiraBaseUrl = jiraBaseUrl;
        this.projectKey = projectKey;
        this.parentKey = parentKey;
        this.jiraAdminUsername = jiraAdminUsername;
        this.jiraApiToken = jiraApiToken;
        this.jiraProgressTransition = Integer.parseInt(jiraProgressTransition);
        this.jiraDoneTransition = Integer.parseInt(jiraDoneTransition);
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
     * Username of a JIRA user with admin privileges - used for authenticating against the JIRA server (eg. "admin").
     */
    public String getJiraAdminUsername()
    {
        return jiraAdminUsername;
    }

    public String getJiraApiToken() {
        return jiraApiToken;
    }

    /**
     * Whether or not diagnostic logging will be printed to the server console.
     */
    public boolean isDebugLoggingEnabled()
    {
        return debugLoggingEnabled;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getParentKey() {
        return parentKey;
    }

    public Integer getJiraProgressTransition() {
        return jiraProgressTransition;
    }

    public Integer getJiraDoneTransition() {
        return jiraDoneTransition;
    }
}
