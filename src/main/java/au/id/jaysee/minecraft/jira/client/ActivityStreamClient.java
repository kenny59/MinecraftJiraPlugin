package au.id.jaysee.minecraft.jira.client;

import org.bukkit.entity.Player;

/**
 *
 */
public interface ActivityStreamClient
{
    public void postActivity(Player actor, String titleHtml, String contentHtml);
}
