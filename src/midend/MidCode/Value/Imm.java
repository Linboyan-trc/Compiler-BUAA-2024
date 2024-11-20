package midend.MidCode.Value;

import backend.ValueMeta;

public class Imm extends Value implements ValueMeta  {
    private final long value;

    public Imm(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String getName() {
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
