package backend.Address;

import backend.ValueMeta.Reg;

public class RelativeAddress implements Address {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个相对地址，10($t0)
    // 1. 由offset(Reg)构成
    private Reg base;
    private int offset;

    public RelativeAddress(Reg base, int offset) {
        this.base = base;
        this.offset = offset;
    }

    public Reg getBase() {
        return base;
    }

    public int getOffset() {
        return offset;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return offset + "(" + base + ")";
    }
}
