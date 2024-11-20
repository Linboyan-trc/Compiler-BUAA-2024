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

    @Override
    public String toString() {
        return entryLabel.toString();
    }
}
