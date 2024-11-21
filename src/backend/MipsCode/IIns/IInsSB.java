package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.ValueMeta.Reg;

public class IInsSB extends IIns {
    private Reg rt;
    private Address address;

    public IInsSB(Reg rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public String toString() {
        return "sb " + rt + ", " + address;
    }
}
