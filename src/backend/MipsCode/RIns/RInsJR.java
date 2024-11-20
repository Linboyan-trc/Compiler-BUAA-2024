package backend.MipsCode.RIns;

import backend.ValueMeta.Reg;

public class RInsJR extends RIns {
    private Reg rs;

    public RInsJR(Reg rs) {
        this.rs = rs;
    }

    @Override
    public String toString() {
        return "jr " + rs;
    }
}
