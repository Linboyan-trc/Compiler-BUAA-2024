package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class Exit implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于在中间代码中添加一行结束节点
    public Exit() {
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 调用完一个函数之后退出
        return "EXIT";
    }
}
