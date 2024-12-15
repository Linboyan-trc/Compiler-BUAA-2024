package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Store extends MidCode implements UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于将值存储到内存中
    private Addr targetValue;
    private Value sourceValue;

    public Store(Addr targetValue, Value sourceValue) {
        this.targetValue = targetValue;
        this.sourceValue = sourceValue;
    }

    public Addr getTargetValue() {
        return targetValue;
    }

    public Value getSourceValue() {
        return sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "*" + targetValue + " <- " + sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public LinkedList<Value> getUseUnit() {
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(targetValue);
        useUnit.add(sourceValue);
        return useUnit;
    }

    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        if (sourceValue == oldValue) {
            sourceValue = newValue;
        } else if (targetValue == oldValue) {
            targetValue = (Addr) newValue;
        }
    }
}
