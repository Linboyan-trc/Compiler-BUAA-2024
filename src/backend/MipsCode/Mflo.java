package backend.MipsCode;

import backend.ValueMeta.Reg;

public class Mflo implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. mflo指令
    private Reg reg;

    public Mflo(Reg reg) {
        this.reg = reg;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return "mflo " + reg;
    }
}
