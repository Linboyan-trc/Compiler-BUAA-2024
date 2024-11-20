package backend.MipsCode.IIns;

import backend.ValueMeta.Reg;
import midend.MidCode.Value.Imm;

public class IInsLI extends IIns {
    private Reg rt;
    private Imm imm;

    public IInsLI(Reg rt, Imm imm) {
        this.rt = rt;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return "li " + rt + ", " + imm;
    }
}
