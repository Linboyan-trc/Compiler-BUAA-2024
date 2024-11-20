package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;

public class Store implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 用于将值存储到内存中
    private final Addr targetValue;
    private final Value sourceValue;

    public Store(Addr targetValue, Value sourceValue) {
        this.targetValue = targetValue;
        this.sourceValue = sourceValue;
        MidCodeTable.getInstance().addToMidCodes(this);
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
}
