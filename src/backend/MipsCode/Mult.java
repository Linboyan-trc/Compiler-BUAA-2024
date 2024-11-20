package backend.MipsCode;

import backend.ValueMeta.Reg;

public class Mult implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. mult指令
    private final Reg rs;
    private final Reg rt;

    public Mult(Reg rs, Reg rt) {
        this.rs = rs;
        this.rt = rt;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return "mult " + rs + ", " + rt;
    }
}
