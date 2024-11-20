package midend.MidCode.Operate;

import midend.MidCode.Value.Value;

public class BinaryOperate implements Operate {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于:+-, */%, &&, ||, <<, >, >=, <, <=, ==, !=
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD, AND, OR, SLL, GT, GE, LT, LE, EQ, NE
    }

    private final BinaryOp binaryOp;
    private final Value leftValue;
    private final Value rightValue;

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
}
