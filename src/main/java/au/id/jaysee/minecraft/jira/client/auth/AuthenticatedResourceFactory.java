package au.id.jaysee.minecraft.jira.client.auth;

import com.sun.jersey.api.client.WebResource;

/**
 * Created by IntelliJ IDEA.
 * User: jclark
 * Date: 28/12/11
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AuthenticatedResourceFactory
{

    public WebResource.Builder getResource(String resourceRelativeURL) throws AuthenticationException;



}
