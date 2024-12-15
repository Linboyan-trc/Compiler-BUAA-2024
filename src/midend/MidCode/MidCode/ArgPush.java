package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class ArgPush extends MidCode implements UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 作为参数传递的变量入栈
    private Value value;

    public ArgPush(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "PUSH " + value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成使用变量
    @Override
    public LinkedList<Value> getUseUnit() {
        // 1. 返回此变量作为使用变量即可
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(value);
        return useUnit;
    }

    // 2. 更换使用变量
    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        // 1. 替换为新的变量
        if (value == oldValue) {
            value = newValue;
        }
    }
}
