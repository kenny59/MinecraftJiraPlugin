package au.id.jaysee;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockListener extends org.bukkit.event.block.BlockListener
{
    Logger log = Logger.getLogger("Minecraft");

    private final McJiraPlugin plugin;
    private final JiraService jira;

    public BlockListener(McJiraPlugin plugin, JiraService jira)
    {
        this.plugin = plugin;
        this.jira = jira;
    }

    private String getSignage(SignChangeEvent event)
    {
        StringBuilder buffer = new StringBuilder();
        for (String line : event.getLines())
        {
            buffer.append(line);
        }
        return buffer.toString();
    }

    @Override
    public void onSignChange(SignChangeEvent event)
    {
        super.onSignChange(event);
        String text = getSignage(event);
        if (text.startsWith("{jira}"))
        {
            text = text.substring(6);
            Block b = event.getBlock();
            jira.CreateIssueAsync(event.getPlayer().getDisplayName(), text, b.getX(), b.getY(), b.getZ());
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        super.onBlockBreak(event);
        Block brokenBlock = event.getBlock();
        if (brokenBlock.getType().equals(Material.SIGN_POST))
        {
            if (brokenBlock.getState() instanceof Sign)
            {
                Sign signage = (Sign) brokenBlock.getState();
                final String issueKeyRegex = "\\{[A-Z]+-[0-9]+}";

                String line = signage.getLine(0);
                Pattern p = Pattern.compile(issueKeyRegex);
                Matcher m = p.matcher(line);
                if (m.matches())
                {
                    final String issueKey = m.group().replace("{", "").replace("}", "");
                    jira.ResolveIssueAsync(issueKey, event.getPlayer().getDisplayName());
                }
            }
        }
    }
}
