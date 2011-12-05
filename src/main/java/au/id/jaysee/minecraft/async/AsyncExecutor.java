package au.id.jaysee.minecraft.async;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Logger;

/**
 * Does stuff on a mother-flipping background thread.
 */
public class AsyncExecutor
{
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final Logger log;

    public AsyncExecutor(final JavaPlugin plugin, final BukkitScheduler scheduler, final Logger log)
    {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.log = log;
    }

    public <T> void executeAsyncTask(final Task<T> task, final Callback<T> callback)
    {
        scheduler.scheduleAsyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                final T result = task.execute();
                scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        callback.execute(result);
                    }
                });
            }
        });
    }
}
