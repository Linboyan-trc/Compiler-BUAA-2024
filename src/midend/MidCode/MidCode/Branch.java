package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class Branch extends MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 跳转操作符:>, >=, <, <=, ==, !=
    // 2. 跳转: 左值 + branchOp + 右值 + 标签
    public enum BranchOp {
        GT, GE, LT, LE, EQ, NE
    }
    private BranchOp branchOp;
    private Value leftValue;
    private Value rightValue;
    private Label branchLabel;

    public Branch(BranchOp branchOp, Value leftValue, Value rightValue, Label branchLabel) {
        this.branchOp = branchOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.branchLabel = branchLabel;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 更改跳转条件
    public void changeBranchOp(Label label) {
        switch (branchOp) {
            case GT:
                branchOp = BranchOp.LE;
                break;
            case GE:
                branchOp = BranchOp.LT;
                break;
            case LT:
                branchOp = BranchOp.GE;
                break;
            case LE:
                branchOp = BranchOp.GT;
                break;
            case EQ:
                branchOp = BranchOp.NE;
                break;
            case NE:
                branchOp = BranchOp.EQ;
                break;
        }
        branchLabel = label;
    }
}
