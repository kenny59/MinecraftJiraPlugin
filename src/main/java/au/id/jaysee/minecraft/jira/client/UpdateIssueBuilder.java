package au.id.jaysee.minecraft.jira.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@SuppressWarnings("unchecked")
final class UpdateIssueBuilder
{
    private Map<String, Object> fieldsToUpdate = new HashMap<String, Object>();

    private UpdateIssueBuilder()
    {

    }

    public static UpdateIssueBuilder get()
    {
        return new UpdateIssueBuilder();
    }

    public UpdateIssueBuilder setField(String fieldName, Object newValue)
    {
        fieldsToUpdate.put(fieldName, newValue);
        return this;
    }

    public JSONObject build()
    {
        JSONObject updateIssue = new JSONObject();
        JSONObject update = new JSONObject();
        updateIssue.put("update", update);

        for (String key : fieldsToUpdate.keySet())
        {
            JSONObject setCommand = new JSONObject();
            setCommand.put("set", fieldsToUpdate.get(key));

            JSONArray field = new JSONArray();
            field.add(setCommand);
            update.put(key, field);
        }
        return updateIssue;
    }
}

