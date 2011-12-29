package au.id.jaysee.minecraft.jira.client;

import org.bukkit.Location;

/**
 *
 */
public class IssueLocation
{
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public IssueLocation(String world, int x, int y, int z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public IssueLocation(Location location)
    {
        this.world = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public String getWorld()
    {
        return world;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }
}
