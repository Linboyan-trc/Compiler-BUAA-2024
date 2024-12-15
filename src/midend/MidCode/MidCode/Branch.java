package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Branch extends MidCode implements UseUnit {
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

    // 2. 化简
    public void simplify() {
        // 1. 左右都是Imm
        if (leftValue instanceof Imm && rightValue instanceof Imm) {
            // 1.1 获取左右Imm
            Imm leftImm = (Imm) leftValue;
            Imm rightImm = (Imm) rightValue;

            // 1.2 记录结果
            boolean result;

            // 1.3 判断结果
            switch (branchOp) {
                case GT:
                    result = leftImm.getValue() > rightImm.getValue();
                    break;
                case GE:
                    result = leftImm.getValue() >= rightImm.getValue();
                    break;
                case LT:
                    result = leftImm.getValue() < rightImm.getValue();
                    break;
                case LE:
                    result = leftImm.getValue() <= rightImm.getValue();
                    break;
                case EQ:
                    result = leftImm.getValue() == rightImm.getValue();
                    break;
                case NE:
                    result = leftImm.getValue() != rightImm.getValue();
                    break;
                default:
                    result = false;
            }

            // 1.4 结果为真替换为Jump
            if (result) {
                this.changeToAnother(new Jump(branchLabel));
            }

            // 1.5 结果不为真直接删除
            else {
                this.removeFromMidCodeList();
            }
        }

        // 2. 左右相等
        else if (leftValue.equals(rightValue)) {
            switch (branchOp) {
                // 2.1 为GT, LT, NE直接替换为Jump
                case GT:
                case LT:
                case NE:
                    this.changeToAnother(new Jump(branchLabel));
                    break;
                // 2.2 为GE, LE, EQ直接删除
                case GE:
                case LE:
                case EQ:
                    this.removeFromMidCodeList();
                    break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public LinkedList<Value> getUseUnit() {
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(leftValue);
        useUnit.add(rightValue);
        return useUnit;
    }

    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        if (leftValue == oldValue) {
            leftValue = newValue;
        }
        if (rightValue == oldValue) {
            rightValue = newValue;
        }
    }
}
