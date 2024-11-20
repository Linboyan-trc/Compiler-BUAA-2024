package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class Return implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 返回值的中间代码
    private Value value = null;

    public Return() {
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    public Return(Value value) {
        this.value = value;
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return value == null ? "RETURN" : "RETURN " + value;
    }
}
