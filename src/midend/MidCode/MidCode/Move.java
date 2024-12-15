package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.Copy;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Move extends MidCode implements DefUnit, UseUnit, Copy {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 赋值的中间代码
    // 1. 是否是临时变量，目标值，源值
    public boolean isTemp;
    public Value targetValue;
    public Value sourceValue;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构造一个Move节点，并在中间代码中自动添加，如果是临时变量则需要添加到变量表
    public Move(boolean isTemp, Value targetValue, Value sourceValue) {
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

    public Value getSourceValue() {
        return sourceValue;
    }

    public void simplify() {
        if (targetValue.equals(sourceValue)) {
            this.removeFromMidCodeList();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return (isTemp ? "TEMP " : "SAVE ") + targetValue.toString() + " <- " + sourceValue.toString();
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
        if (sourceValue == oldValue) {
            sourceValue = newValue;
        }
    }

    @Override
    public LinkedList<Value> getSource() {
        LinkedList<Value> source = new LinkedList<>();
        source.add(sourceValue);
        return source;
    }

    @Override
    public Value getTarget() {
        return targetValue;
    }
}
