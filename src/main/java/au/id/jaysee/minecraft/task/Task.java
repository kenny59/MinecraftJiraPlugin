package au.id.jaysee.minecraft.task;

import java.net.URISyntaxException;

/**
 * Implemented anonymously by callers of the {@link TaskExecutor#executeAsyncTask(Task, Callback)} method to provide a
 * delegate that is invoked asynchronously on a background thread in order to avoid blocking the main Minecraft thread.
 * The result returned from the {@link #execute()} method is passed in to the corresponding {@link Callback#execute(Object)}
 * method once the async task has completed.
 *
 * @author Joe Clark
 * @see {@link TaskExecutor}
 * @since 1.0
 */
public interface Task<T>
{
    /**
     * Invokes the asynchronous task.
     * @return The value to be passed back to the foreground callback.
     */
    public T execute() throws URISyntaxException;
}
