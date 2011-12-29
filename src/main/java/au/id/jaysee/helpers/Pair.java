package au.id.jaysee.helpers;

/**
 * An encapsulation for a pair of values (a "left" value and a "right" value).
 *
 * @author Joe Clark
 * @since  1.0
 */
public final class Pair<L, R>
{
    private final L left;
    private final R right;

    /**
     * Constructs the pair
     *
     * @param left  the left value
     * @param right the right value
     */
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    /**
     * @return Returns the left value.
     */
    public L getLeft()
    {
        return left;
    }

    /**
     * @return Returns the right value.
     */
    public R getRight()
    {
        return right;
    }
}
