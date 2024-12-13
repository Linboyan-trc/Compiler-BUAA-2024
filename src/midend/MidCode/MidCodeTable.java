package midend.MidCode;

import backend.Block.FuncBlock;
import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;
import midend.MidCode.MidCode.*;
import midend.MidCode.Value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;

public class MidCodeTable {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 单例模式
    private static final MidCodeTable instance = new MidCodeTable();

    // 2. 初始默认当前函数为'@global' + 全局变量 + 函数表 = 函数名+变量表
    private String func = "@global";
    private final LinkedList<MidCode> globalCodes = new LinkedList<>();
    private final HashMap<String, LinkedList<Value>> funcToVals = new HashMap<>();
    // 2.2 中间代码 + 变量表(每个变量会和id关联，有唯一性保证)
    private final LinkedList<MidCode> midCodes = new LinkedList<>();
    private final HashMap<Value, Integer> valToSize = new HashMap<>();

    // 3. 循环
    private final LinkedList<Label> loopBeginLabels = new LinkedList<>();
    private final LinkedList<Label> loopEndLabels = new LinkedList<>();

    // 4. 新定义的
    private final LinkedList<FuncBlock> funcBlockList = new LinkedList<>();
    private final HashSet<Label> loopMark = new HashSet<>();
    private final MidCode head = new Nop();
    private MidCode tail = head;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构造
    private MidCodeTable() {
        funcToVals.put("@global", new LinkedList<>());
    }

    // 1. 获取单例
    public static MidCodeTable getInstance() {
        return instance;
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

    // 3. 获取全局变量
    public LinkedList<MidCode> getGlobalCodeList() {
        return globalCodes;
    }

    // 4. 获取中间代码
    public LinkedList<MidCode> getMidCodeList() {
        return midCodes;
    }

    // 5. 获取变量表
    public LinkedList<Value> getValInfos(String func) {
        return funcToVals.get(func);
    }

    // 6. 获取变量空间
    public int getValSize(Value value) {
        return valToSize.get(value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 设置当前函数，并且在(函数名+变量表)中添加
    public void setFunc(String func) {
        // 1. 设置当前函数
        this.func = func;
        // 2. 如果是一个新的函数，就准备一个空列表存函数的参数
        if (!funcToVals.containsKey(func)) {
            funcToVals.put(func, new LinkedList<>());
        }
    }

    public void setLoop(Label loopBegin, Label loopEnd) {
        loopBeginLabels.add(loopBegin);
        loopEndLabels.add(loopEnd);
    }

    public void unsetLoop() {
        loopBeginLabels.removeLast();
        loopEndLabels.removeLast();
    }

    public void markLoop(Label stmtBegin) {
        loopMark.add(stmtBegin);
    }

    public HashSet<Label> getLoopMark() {
        return loopMark;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 添加中间代码
    public void addToMidCodes(MidCode midCode) {
        // 1. 在DeclNode的时候，是加入到globalCodes
        if (func.equals("@global")) {
            globalCodes.add(midCode);
        }
        // 2. 到main函数的时候，func是main
        // 2. 加入到midCodes
        else if (func.equals("main") && midCode instanceof Return) {
            Exit exit = new Exit();
            midCodes.add(exit);
            tail = tail.link(exit);
        }
        else {
            midCodes.add(midCode);
            tail = tail.link(midCode);
        }
    }

    // 2. 添加到变量表
    public void addToVarInfo(Value value, int size) {
        // 1. 在函数作用于内的变量，添加到函数,变量表
        funcToVals.get(func).add(value);

        // 2. 记录变量尺寸
        valToSize.put(value, size);
    }

    // 3. 化简
    public void simplify() {
        simplifyNop();
        simplifyLabel();
        simplifyExp();
    }

    // 3.1 化简Nop
    public void simplifyNop() {
        int index;
        for (MidCode midCode : midCodes) {
            if (midCode instanceof Nop) {
                index = midCodes.indexOf(midCode);
                for (Label label : LabelTable.getInstance().getLabelList(midCode)) {
                    label.setMidCode(midCodes.get(index + 1));
                }
                midCodes.remove(index);
            }
        }
    }

    // 3.2 化简Label
    public void simplifyLabel() {
        int index;
        HashSet<Label> usedLabels = new HashSet<>();
        for (MidCode midCode : midCodes) {
            if (midCode instanceof Jump) {
                Jump jump = (Jump) midCode;
                Label target = jump.getLabel().getTarget();
                index = midCodes.indexOf(jump);
                if (midCodes.indexOf(target.getMidCode()) - index == 1) {
                    midCodes.remove(index);
                } else {
                    jump.setLabel(target);
                    usedLabels.add(target);
                }
            } else if (midCode instanceof Branch) {
                Branch branch = (Branch) midCode;
                Label target = branch.getBranchLabel().getTarget();
                index = midCodes.indexOf(branch);
                if (midCodes.indexOf(target.getMidCode()) - index == 1) {
                    midCodes.remove(index);
                } else {
                    branch.setLabel(target);
                    usedLabels.add(target);
                }
            }
        }
        LabelTable.getInstance().removeUnusedLabels(usedLabels);
    }

    // 3.3 化简Exp
    public void simplifyExp() {
        for (MidCode midCode : midCodes) {
            if (midCode instanceof Assign) {
                ((Assign) midCode).simplify();
            } else {

            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 中间代码
        StringBuilder stringBuilder = new StringBuilder();

        // 2. gloablCodes
        for (MidCode midCode : globalCodes) {
            stringBuilder.append(midCode.toString()).append("\n");
        }

        // 3. midCodes
        for (MidCode midCode : midCodes) {
            // 3.1 midCode可能是FuncCall
            // 3.1 查找FuncCall在标签表中是否有标签，有的话在调用函数前要插入标签，用于跳转
            // 3.1 main:
            // 3.2 但在midCodes中没有这个标签，midCodes中为:
            // 3.2 CALL main<FuncCall>, EXIT<Exit>, main:<FuncEntry>, RETURN 0<Return>
            for (Label label : LabelTable.getInstance().getLabelList(midCode)) {
                stringBuilder.append(label.toString()).append("\n");
            }

            // 3. 生成函数入口，以及函数体
            // 3. main:
            // 3. RETURN 0
            stringBuilder.append(midCode.toString()).append("\n");
        }

        return stringBuilder.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public LinkedList<FuncBlock> getFuncBlockList() {
        return funcBlockList;
    }
}




















