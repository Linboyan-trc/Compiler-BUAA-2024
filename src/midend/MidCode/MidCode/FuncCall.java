package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class FuncCall implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于调用一个函数
    public final String name;

    // 2. 创建函数调用节点时自动在中间代码表中添加一行调用代码
    public FuncCall(String name) {
        this.name = name;
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "CALL " + name;
    }
}
