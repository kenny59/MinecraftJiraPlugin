package au.id.jaysee.minecraft.jira.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 */
final class TransitionBuilder
{
    private TransitionBuilder()
    {

    }

    @SuppressWarnings("unchecked")
    public JSONObject build()
    {
        /**
         * {
         *      "update": {
         *          "comment" : {
         *              "add" : {
         *                  "body" : "Issue resolved by %s"
         *              }
         *          }
         *      },
         *      "fields": {
         *          "resolution": {
         *              "name" : "Fixed"
         *          }
         *      },
         *      "transition" : {
         *          "name" : "Resolve Issue"
         *      }
         * }
         */

        JSONObject transitionCommand = new JSONObject();
        JSONObject update = new JSONObject();
        transitionCommand.put("update", update);

        // comment: [ ]
        JSONArray comment = new JSONArray();
        update.put("comment", comment);

        JSONObject addCommentObject = new JSONObject();



        JSONObject add = new JSONObject();
        add.put("body", commentBody);
        addCommentObject.put("add", add);

        comment.add(addCommentObject);

        JSONObject fields = new JSONObject();
        transitionCommand.put("fields", fields);
        JSONObject resolution = new JSONObject();
        resolution.put("name", resolutionName);
        fields.put("resolution", resolution);

        JSONObject transition = new JSONObject();
        transition.put("id", String.valueOf(transitionId));
        transitionCommand.put("transition", transition);

        return transitionCommand;
    }

    public static TransitionBuilder get()
    {
        return new TransitionBuilder();
    }

    private String commentBody;
    private String resolutionName;
    private long transitionId;

    public TransitionBuilder setResolution(String resolutionName)
    {
        this.resolutionName = resolutionName;
        return this;
    }

    public TransitionBuilder setTransition(long transitionId)
    {
        this.transitionId = transitionId;
        return this;
    }

    public TransitionBuilder addComment(String commentBody)
    {
        this.commentBody = commentBody;
        return this;
    }
}
