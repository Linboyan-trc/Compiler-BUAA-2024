package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.ValueMeta.Reg;

public class IInsLA extends IIns {
    private Reg rt;
    private Address address;

    public IInsLA(Reg rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public String toString() {
        return "la " + rt + ", " + address;
    }
}
