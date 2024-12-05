package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCodeTable;

public class FuncEntry implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 函数入口的中间代码节点
    // 1. 关联一个Label
    private final Label entryLabel;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 创建一个函数入口的中间代码节点，指定标签，然后自动添加到中间代码中
    public FuncEntry(Label entryLabel) {
        this.entryLabel = entryLabel;
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    public Label getEntryLabel() {
        return entryLabel;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 直接用这个入口关联的标签生成字符串
        return entryLabel.toString();
    }
}
