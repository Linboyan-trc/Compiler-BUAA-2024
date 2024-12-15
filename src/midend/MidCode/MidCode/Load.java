package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Load extends MidCode implements DefUnit, UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 从内存中加载变量
    private boolean isTemp;
    private Value targetValue;
    private Addr sourceValue;

    public Load(boolean isTemp, Value targetValue, Addr sourceValue) {
        this.isTemp = isTemp;
        this.targetValue = targetValue;
        this.sourceValue = sourceValue;
    }

    public boolean isTemp() {
        return isTemp;
    }

    public Value getTargetValue() {
        return targetValue;
    }

    public Addr getSourceValue() {
        return sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return (isTemp ? "TEMP " : "SAVE ") + targetValue + " <- *" + sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成定义变量
    @Override
    public Value getDefUnit() {
        // 1. 直接返回左值即可
        return targetValue;
    }

    @Override
    public LinkedList<Value> getUseUnit() {
        LinkedList<Value> useUnit = new LinkedList<>();
        useUnit.add(sourceValue);
        return useUnit;
    }

    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        if (sourceValue.equals(oldValue)) {
            sourceValue = (Addr) newValue;
        }
    }
}
