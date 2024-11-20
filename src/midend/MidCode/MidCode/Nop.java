package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class Nop implements MidCode {
    public Nop() {
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    @Override
    public String toString() {
        return "NOP";
    }
}
