package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Operate.Operate;
import midend.MidCode.Value.Value;

public class Assign implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 不用加载，直接变量之间的赋值
    // 1. 是否是临时变量 + 目标值 + 源值
    private final boolean isTemp;
    private final Value targetValue;
    private final Operate sourceValue;

    public Assign(boolean isTemp, Value targetValue, Operate sourceValue) {
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

    public Operate getSourceValue() {
        return sourceValue;
    }

    public void simplify() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return targetValue + " <- " + sourceValue;
    }
}
