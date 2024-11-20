package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.ValueMeta.Reg;

public class IInsLW extends IIns {
    private Reg rt;
    private Address address;

    public IInsLW(Reg rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public String toString() {
        return "lw " + rt + ", " + address;
    }
}
