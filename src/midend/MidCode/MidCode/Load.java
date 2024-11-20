package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;

public class Load implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 从内存中加载变量
    private final boolean isTemp;
    private final Value targetValue;
    private final Addr sourceValue;

    public Load(boolean isTemp, Value targetValue, Addr sourceValue) {
        this.isTemp = isTemp;
        this.targetValue = targetValue;
        this.sourceValue = sourceValue;
        MidCodeTable.getInstance().addToMidCodes(this);
        if (isTemp) {
            MidCodeTable.getInstance().addToVarInfo(targetValue, 1);
        }
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
}
