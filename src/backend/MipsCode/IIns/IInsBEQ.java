package backend.MipsCode.IIns;

import backend.ValueMeta.Reg;

public class IInsBEQ extends IIns {
    private Reg rs;
    private Reg rt;
    private String label;

    public IInsBEQ(Reg rs, Reg rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    @Override
    public String toString() {
        return "beq " + rs + ", " + rt + ", " + label;
    }
}
