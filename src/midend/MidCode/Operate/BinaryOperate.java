package midend.MidCode.Operate;

import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class BinaryOperate implements Operate {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于:+-, */%, &&, ||, <<, >, >=, <, <=, ==, !=
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD, AND, OR, SLL, GT, GE, LT, LE, EQ, NE
    }

    private BinaryOp binaryOp;
    private Value leftValue;
    private Value rightValue;

    public BinaryOperate(BinaryOp binaryOp, Value leftValue, Value rightValue) {
        this.binaryOp = binaryOp;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public BinaryOp getBinaryOp() {
        return binaryOp;
    }

    public Value getLeftValue() {
        return leftValue;
    }

    public Value getRightValue() {
        return rightValue;
    }

    @Override
    public String toString() {
        return leftValue.toString() + " " + binaryOp.toString() + " " + rightValue.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成使用变量
    @Override
    public LinkedList<Value> getUseUnit() {
        // 1. 左值，右值都作为使用变量
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(leftValue);
        useUnit.add(rightValue);
        return useUnit;
    }

    // 1. 更换使用变量
    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        // 1. 左值，右值中若有需要更换的使用变量，就要更换使用变量
        if (leftValue == oldValue) {
            leftValue = newValue;
        }
        if (rightValue == oldValue) {
            rightValue = newValue;
        }
    }
}
