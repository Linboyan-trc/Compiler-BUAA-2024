package backend.MipsCode.RIns;

import backend.ValueMeta.Reg;
import midend.MidCode.Value.Imm;

public class RIns2Reg1Imm extends RIns {
    public RInsOpcode opcode;
    private Reg rd;
    private Reg rt;
    private Imm shamt;

    public RIns2Reg1Imm(RInsOpcode opcode, Reg rd, Reg rt, Imm shamt) {
        this.opcode = opcode;
        this.rd = rd;
        this.rt = rt;
        this.shamt = shamt;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + rd + ", " + rt + ", " + shamt;
    }
}
