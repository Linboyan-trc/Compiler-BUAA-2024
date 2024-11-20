package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.ValueMeta.Reg;

public class IInsSW extends IIns {
    private Reg rt;
    private Address address;

    public IInsSW(Reg rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public String toString() {
        return "sw " + rt + ", " + address;
    }
}
