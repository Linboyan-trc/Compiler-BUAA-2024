package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Value.*;

public class IntGet extends MidCode implements DefUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 读取一个int
    public IntGet() { }

    @Override
    public String toString() {
        return "CALL GETINT";
    }

    @Override
    public Value getDefUnit() {
        return new Word("?");
    }
}
