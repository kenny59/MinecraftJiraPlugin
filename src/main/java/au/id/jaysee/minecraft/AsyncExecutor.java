package au.id.jaysee.minecraft;

import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Logger;

/**
 * Does stuff on a mother-flipping background thread.
 */
public class AsyncExecutor
{
    private final BukkitScheduler scheduler;
    private final Logger log;

    public AsyncExecutor(final BukkitScheduler scheduler, final Logger log)
    {
        this.scheduler = scheduler;
        this.log = log;
    }

    public interface AsyncCallback
    {
        public void doIt();
    }



    public void executeAsyncTask(AsyncCallback executeAsynchronously, )


}
