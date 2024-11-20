package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class IntGet implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 读取一个int
    public IntGet() {
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    @Override
    public String toString() {
        return "CALL GETINT";
    }
}
