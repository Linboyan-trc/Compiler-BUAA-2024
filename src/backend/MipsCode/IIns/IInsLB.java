package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.ValueMeta.Reg;

public class IInsLB extends IIns {
    private Reg rt;
    private Address address;

    public IInsLB(Reg rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public String toString() {
        return "lb " + rt + ", " + address;
    }
}
