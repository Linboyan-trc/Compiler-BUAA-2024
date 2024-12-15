package midend.MidCode.Operate;

import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class UnaryOperate implements Operate {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 单变量运算:+, -, !
    public enum UnaryOp {
        POS, NEG, NOT
    }

    private UnaryOp unaryOp;
    private Value value;

    public UnaryOperate(UnaryOp unaryOp, Value value) {
        this.unaryOp = unaryOp;
        this.value = value;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return unaryOp.toString() + " " + value.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public LinkedList<Value> getUseUnit() {
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(value);
        return useUnit;
    }

    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        if (value == oldValue) {
            value = newValue;
        }
    }
}
