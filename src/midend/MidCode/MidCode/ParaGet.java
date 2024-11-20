package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class ParaGet implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 一个参数具有一个Value:Word, Addr, Imm
    private final Value value;

    public ParaGet(Value value) {
        this.value = value;
        MidCodeTable.getInstance().addToMidCodes(this);
        MidCodeTable.getInstance().addToVarInfo(value, 1);
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "PARA " + value;
    }
}
