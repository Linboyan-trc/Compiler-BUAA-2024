package backend.MipsCode.IIns;

import backend.ValueMeta.Reg;
import midend.MidCode.Value.Imm;

public class IIns2Reg1Imm extends IIns {
    private IInsOpcode opcode;
    private Reg rs;
    private Reg rt;
    private Imm imm;

    public IIns2Reg1Imm(IInsOpcode opcode, Reg rs, Reg rt, Imm imm) {
        this.opcode = opcode;
        this.rs = rs;
        this.rt = rt;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + rs + ", " + rt + ", " + imm;
    }
}
