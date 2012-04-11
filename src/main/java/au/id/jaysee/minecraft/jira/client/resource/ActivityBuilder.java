package au.id.jaysee.minecraft.jira.client.resource;

import org.json.simple.JSONObject;

/**
 *
 */
public final class ActivityBuilder
{
    public static ActivityBuilder get()
    {
        return new ActivityBuilder();
    }

    private String actor; // JIRA Username
    private String iconUrl = "http://www.minecraft.net/favicon.png"; // TODO: Don't hardcode this to minecraft.net.
    private String title;
    private String content;
    private String id;


    /**
     * Only the {@link #get()} method constructs new instances.
     */
    private ActivityBuilder()
    {
        // empty
    }

    public ActivityBuilder setId(String id)
    {
        this.id = id;
        return this;
    }

    public ActivityBuilder setActor(String id)
    {
        this.actor = id;
        return this;
    }

    public ActivityBuilder setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public ActivityBuilder setContent(String content)
    {
        this.content = content;
        return this;
    }

    public JSONObject build()
    {
        JSONObject activity = new JSONObject();
        JSONObject actor = new JSONObject();
        actor.put("id", this.actor);
        activity.put("actor", actor);

        JSONObject generator = new JSONObject();
        generator.put("id", "http://minecraft.net");
        generator.put("displayName", "Minecraft");
        activity.put("generator", generator);

        JSONObject icon = new JSONObject();
        icon.put("url", iconUrl);
        icon.put("width", "16");
        icon.put("height", "16");
        activity.put("icon", icon);

        activity.put("id", this.id);
        activity.put("title", this.title);
        activity.put("content", this.content);

        return activity;
    }



}
