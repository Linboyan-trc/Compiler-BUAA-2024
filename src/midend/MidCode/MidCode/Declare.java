package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class Declare implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 声明一个变量的中间代码
    // 1. 是否是全局变量 + 是否是常量
    private final boolean isGlobal;
    private final boolean isFinal;
    // 2. Value:Word,Addr,Imm三种 + 大小:变量默认为1，数组的话就是数组长度 + 初始值:Word,Addr,Imm列表
    private final Value value;
    private final int size;
    private final LinkedList<Value> initValues;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 创建一个声明节点，并自动添加到中间代码中
    public Declare(boolean isGlobal,
                   boolean isFinal,
                   Value value,
                   int size,
                   LinkedList<Value> initValues) {
        // 1. 创建一个变量声明实例Declare
        this.isGlobal = isGlobal;
        this.isFinal = isFinal;
        this.value = value;
        this.size = size;
        this.initValues = initValues;

        // 2. 在中间代码中添加变量节点
        MidCodeTable.getInstance().addToMidCodes(this);

        // 3. 在变量表中记录变量
        MidCodeTable.getInstance().addToVarInfo(value, size);
    }

    // 1. 获取变量名:Value, '$'a1@0, '&'a2@0
    public Value getValue() {
        return value;
    }

    // 2. 获取变量字长度
    public int getSize() {
        return size;
    }

    // 3. 获取变量初始值
    public LinkedList<Value> getInitValues() {
        return initValues;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. initValues转换成字符串: 1,2,3
        StringBuilder initValuesString = new StringBuilder();
        if(initValues != null && !initValues.isEmpty()) {
            for (int i = 0; i < initValues.size(); i++) {
                initValuesString.append(initValues.get(i).toString());
                if (i != initValues.size() - 1) {
                    initValuesString.append(", ");
                }
            }
        }

        // 2. 全局变量:GLOBAL, 非全局变量:LOCAL
        // 2. 常量:CONST,     非常量:VAR
        // 2. 加上:Word, $a1@0
        // 2. 加上:initValues 1
        return (isGlobal ? "GLOBAL" : "LOCAL") + " " +
                (isFinal ? "CONST" : "VAR") + " " +
                value + " " +
                initValuesString.toString();
    }
}
