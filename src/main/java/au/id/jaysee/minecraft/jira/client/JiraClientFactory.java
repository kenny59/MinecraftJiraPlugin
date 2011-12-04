package au.id.jaysee.minecraft.jira.client;

/**
 *
 */
public interface JiraClientFactory {

    public JiraClient getClient(String jiraBaseUrl, String minecraftProjectKey, String adminUsername, String adminPassword);

}
