package au.id.jaysee.minecraft.async;

/**
 *
 */
public interface Callback<T>
{
    public void execute(T input);
}
