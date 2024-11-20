package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class Branch implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 跳转操作符:>, >=, <, <=, ==, !=
    // 2. 跳转: 左值 + branchOp + 右值 + 标签
    public enum BranchOp {
        GT, GE, LT, LE, EQ, NE
    }
    private final BranchOp branchOp;
    private final Value leftValue;
    private final Value rightValue;
    private Label branchLabel;

    public Branch(BranchOp branchOp, Value leftValue, Value rightValue, Label branchLabel) {
        this.branchOp = branchOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.branchLabel = branchLabel;
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    public BranchOp getBranchOp() {
        return branchOp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Label getBranchLabel() {
        return branchLabel;
    }

    public void setLabel(Label target) {
        this.branchLabel = target;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "BRANCH " + branchLabel + " IF " + leftValue + " " + branchOp + " " + rightValue;
    }
}
