package au.id.jaysee.minecraft.task;

/**
 * Implemented anonymously by callers of the {@link TaskExecutor#executeAsyncTask(Task, Callback)} method to provide a
 * delegate that is invoked upon the completion of the asynchronous task. The callback is marshalled onto the foreground
 * thread before executing.
 *
 * @author Joe Clark
 * @see {@link TaskExecutor}
 * @since 1.0
 */
public interface Callback<T>
{
    /**
     * Invokes the callback. The input parameter is the value returned from the corresponding {@link Task}.
     *
     * @param input The result returned from the asynchronous task.
     */
    public void execute(T input);
}
