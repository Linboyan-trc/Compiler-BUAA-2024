package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class Move extends MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 赋值的中间代码
    // 1. 是否是临时变量，目标值，源值
    public final boolean isTemp;
    public final Value targetValue;
    public final Value sourceValue;

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

}
