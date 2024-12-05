package midend.MidCode.Value;

import backend.ValueMeta.ValueMeta;

public class Imm extends Value implements ValueMeta  {
    private long value;

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
    public Void truncTo8(){
        value = value & 0xFF;
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
