package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class ParaGet extends MidCode implements DefUnit, UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 一个参数具有一个Value:Word, Addr, Imm
    private Value value;

    public ParaGet(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "PARA " + value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成定义变量
    @Override
    public Value getDefUnit() {
        // 1. 直接返回变量即可
        return value;
    }

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