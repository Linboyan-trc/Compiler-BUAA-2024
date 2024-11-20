package backend.MipsCode.IIns;

import backend.ValueMeta.Reg;

public class IInsBNE extends IIns {
    private Reg rs;
    private Reg rt;
    private String label;

    public IInsBNE(Reg rs, Reg rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    @Override
    public String toString() {
        return "bne " + rs + ", " + rt + ", " + label;
    }
}
