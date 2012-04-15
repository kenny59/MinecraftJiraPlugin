package au.id.jaysee.minecraft.jira.client;

import au.id.jaysee.jira.client.JiraSoapService;
import au.id.jaysee.jira.client.JiraSoapServiceServiceLocator;
import au.id.jaysee.minecraft.config.Configuration;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 *
 */
public class JiraUserClient
{
    private static final String JIRA_ENDPOINT_FORMAT = "%s/rpc/soap/jirasoapservice-v2";
    private JiraSoapService service;
    private String token;
    private final Logger log;

    public JiraUserClient(Logger log, Configuration pluginConfig)
    {
        this.log = log;
        primeSoapClient(pluginConfig);
    }

    public boolean doesJiraUserExist(String username)
    {
        log.info("Checking if user " + username + " exists in JIRA");
        try
        {
            boolean result = service.getUser(token, username) != null;
            log.info("Result: " + result);
            return result;
        }
        catch (RemoteException e)
        {
            log.info("Check failed: " + e.getMessage());
            return false;
        }
    }

    public void createUser(String username)
    {
        log.info("Creating new user for " + username);
        String securePassword = "minecraft"; // TODO: Generate this password securely.
        try
        {
            service.createUser(token, username, securePassword, "Some Minecraft User", "example@example.com");
            log.info("Create user succeeded");
        } catch (RemoteException e)
        {
            log.info("Create user failed");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private boolean primeSoapClient(Configuration pluginConfig)
    {
        if (service == null)
        {
            JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
            locator.setJirasoapserviceV2EndpointAddress(String.format(JIRA_ENDPOINT_FORMAT, pluginConfig.getJiraBaseUrl()));
            try
            {
                service = locator.getJirasoapserviceV2();
            } catch (ServiceException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            try
            {
                token = service.login(pluginConfig.getJiraAdminUsername(), pluginConfig.getJiraAdminPassword());
            } catch (RemoteException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return true;
    }


}
