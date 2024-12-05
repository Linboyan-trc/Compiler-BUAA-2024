package midend.MidCode.Value;

public abstract class Value {
    public static int tempCnt = 0;

    public abstract String getName();

    public abstract Void truncTo8();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            return toString().equals(obj.toString());
        } else {
            return false;
        }
    }

    @Override public int hashCode()
    {
        return toString().hashCode();
    }
}
