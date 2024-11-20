package backend.MipsCode.RIns;

import backend.ValueMeta.Reg;

public class RIns3Reg extends RIns {
    public final RInsOpcode opcode;
    private Reg rd;
    private Reg rs;
    private Reg rt;

    public RIns3Reg(RInsOpcode opcode, Reg rd, Reg rs, Reg rt) {
        this.opcode = opcode;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + rd + ", " + rs + ", " + rt;
    }

}
