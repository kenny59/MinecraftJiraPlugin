package au.id.jaysee.helpers;

/**
 *
 */
public final class Either<First, Second>
{
    private final First first;
    private final Second second;

    public Either(First first, Second second)
    {
        this.first = first;
        this.second = second;
    }

    public First getFirst()
    {
        return first;
    }

    public Second getSecond()
    {
        return second;
    }
}
