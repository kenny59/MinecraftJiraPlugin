package au.id.jaysee.minecraft.task;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * <p/>
 * Defines a helpful component that is able to perform tasks on a background thread and then marshall a callback back
 * on to the Minecraft main thread for performing an action such as informing a player of the result.
 * <p/>
 * As a game built around a high-performance 3D rendering engine, care must be taken to avoid doing any processing on
 * Minecraft's main UI thread that may block or spin-lock for long periods of time. This would cause the game to "hang"
 * and appear unresponsive to the user. However, attempting to interact with or manipulate the Minecraft environment
 * from outside the main thread creates a major problem for concurrency issues.
 * <p/>
 * Thus, the best approach is to offload as much work as possible to a background thread, reserving the main thread for
 * only work which simply _must_ be done in the foreground (such as updating the state of blocks in the world or generally
 * interacting with the world in any non read-only way).
 * <p/>
 * The Bukkit API provides a {@link BukkitScheduler} component for performing this kind of work, but it is cumbersome to
 * write code that runs asynchronously on a background thread and then marshal synchronously back on to the main thread.
 *
 * This class makes it much easier:
 * {code}
 *   private final TaskExecutor taskExecutor; // get this from the {@link au.id.jaysee.minecraft.McJiraPlugin}
 *   public void doSomeWork(Player aLoggedInPlayer)
 *   {
 *       taskExecutor.executeAsyncTask(new Task&lt;String&gt;()
 *       {
 *           // This code is executed asynchronously on a background thread.
 *           @Override
 *           public String execute()
 *           {
 *                // Do some intensive calculation, interaction with a remote process or some other resource-intensive
 *                // or IO-bound operation.
 *                return "Hello World";
 *           }
 *       }, new Callback&lt;String&gt;()
 *       {
 *           // This code is executed synchronously on the main thread. The value of the input parameter is the value
 *           // returned from the execute method above.
 *           @Override
 *           public void execute(String input)
 *           {
 *              // TODO: double-check that the user is still logged in now, since some time may have elapsed while waiting
 *              // for the asynchronous task to complete.
 *              aLoggedInPlayer.chat(input);
 *           }
 *       });
 *   }
 * {code}
 *
 * @author Joe Clark
 * @since 1.0
 * @see <a href="http://wiki.bukkit.org/Scheduler_Programming">Bukkit Wiki: Scheduler Programming</a>
 * @see {@link Callback}
 * @see {@link Task}
 */
public final class TaskExecutor
{
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;

    /**
     * Constructs the TaskExecutor.
     *
     * @param plugin    The root plugin object.
     * @param scheduler The bukkit scheduler
     */
    public TaskExecutor(final JavaPlugin plugin, final BukkitScheduler scheduler)
    {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    /**
     * Provides a convenient method for executing an asynchronous task on a background thread with a callback that is
     * automatically marshalled back on to the main thread when the async task is complete.
     *
     * @param task     The code to be executed in the background.
     * @param callback The callback to be executed in the foreground.
     * @param <T>      The type parameter specifies the type of the value returned from the {@link au.id.jaysee.minecraft.task.Task#execute()} method,
     *                 which is passed into the {@link Callback#execute(Object)} method as the input parameter.
     */
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
