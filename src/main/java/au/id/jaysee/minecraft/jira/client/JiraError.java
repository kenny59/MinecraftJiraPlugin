package au.id.jaysee.minecraft.jira.client;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class JiraError
{
    private final Collection<String> errorMessages;

    public JiraError(Collection<String> errorMessages)
    {
        this.errorMessages = new ArrayList<String>();
        this.errorMessages.addAll(errorMessages);
    }

    public void addErrorMessage(String errorMessage)
    {
        errorMessages.add(errorMessage);
    }

    public Collection<String> getErrorMessages()
    {
        return errorMessages;
    }
}
