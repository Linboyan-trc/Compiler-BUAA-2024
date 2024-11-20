package midend.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCode.MidCode;
import midend.MidCode.Value.Value;

import java.util.HashMap;
import java.util.LinkedList;

public class MidCodeTable {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 单例模式
    private static final MidCodeTable instance = new MidCodeTable();

    // 2. 初始默认当前函数为'@global' + 全局变量 + 函数表 = 函数名+变量表
    private static String func = "@global";
    private static final LinkedList<MidCode> globalCodes = new LinkedList<>();
    private static final HashMap<String, LinkedList<Value>> funcToVals = new HashMap<>();
    // 2.2 中间代码 + 变量表(每个变量会和id关联，有唯一性保证)
    private static final LinkedList<MidCode> midCodes = new LinkedList<>();
    private static final HashMap<Value, Integer> valToSize = new HashMap<>();

    // 3. 循环
    private static final LinkedList<Label> loopBeginLabels = new LinkedList<>();
    private static final LinkedList<Label> loopEndLabels = new LinkedList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 获取单例
    public static MidCodeTable getInstance() {
        return instance;
    }

    // 2. 加载类的时候执行
    static {
        funcToVals.put("@global", new LinkedList<>());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 获取标签栈
    public Label getLoopBegin() {
        return loopBeginLabels.getLast();
    }

    // 2. 获取标签栈
    public Label getLoopEnd() {
        return loopEndLabels.getLast();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 设置当前函数，并且在(函数名+变量表)中添加
    public void setFunc(String func) {
        this.func = func;
        funcToVals.putIfAbsent(func, new LinkedList<>());
    }

    public void setLoop(Label loopBegin, Label loopEnd) {
        loopBeginLabels.add(loopBegin);
        loopEndLabels.add(loopEnd);
    }

    public void unsetLoop() {
        loopBeginLabels.removeLast();
        loopEndLabels.removeLast();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 添加中间代码
    public void addToMidCodes(MidCode midCode) {
        if (func.equals("@global")) {
            globalCodes.add(midCode);
        } else {
            midCodes.add(midCode);
        }
    }

    // 2. 添加到变量表
    public void addToVarInfo(Value value, int size) {
        funcToVals.get(func).add(value);
        valToSize.put(value, size);
    }
}
