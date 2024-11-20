package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class CharGet implements MidCode {
    public CharGet() {
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    @Override
    public String toString() {
        return "CALL GETCHAR";
    }
}
