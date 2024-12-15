package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Return extends MidCode implements UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 返回值的中间代码
    private Value value = null;

    public Return() { }

    public Return(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 没有返回值，就是RETURN
        // 2. 有返回值，就是RETURN + 返回值.toString
        // 2. 返回值是Value类型，Imm，字符串就是Number转换成Imm的值
        return value == null ? "RETURN" : "RETURN " + value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public LinkedList<Value> getUseUnit() {
        LinkedList<Value> useUnit = new LinkedList<>();
        if (value != null) {
            useUnit.add(value);
        }
        return useUnit;
    }

    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        if (value == oldValue) {
            value = newValue;
        }
    }
}
