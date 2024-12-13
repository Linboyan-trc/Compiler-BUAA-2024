package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class ArgPush implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 作为参数传递的变量入栈
    private final Value value;

    public ArgPush(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "PUSH " + value;
    }
}
