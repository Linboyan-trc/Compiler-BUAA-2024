package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class FuncCall extends MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于调用一个函数
    public final String name;

    // 2. 创建函数调用节点时自动在中间代码表中添加一行调用代码
    public FuncCall(String name) {
        // 1. 指定函数名字
        this.name = name;
    }

    // 3. 获取函数名
    public String getName() {
        return name;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 函数调用的生成中间代码: CALL + 函数名
        return "CALL " + name;
    }
}
