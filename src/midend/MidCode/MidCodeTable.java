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
    private final LinkedList<FuncBlock> funcBlocks = new LinkedList<>();
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
            tail = tail.linkToNext(exit);
        }
        else {
            midCodes.add(midCode);
            tail = tail.linkToNext(midCode);
        }
    }

    // 2. 添加到变量表
    public void addToVarInfo(Value value, int size) {
        // 1. 在函数作用于内的变量，添加到函数,变量表
        funcToVals.get(func).add(value);

        // 2. 记录变量尺寸
        valToSize.put(value, size);
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
    // 1. 化简
    public void simplify() {
        // 1. 化简
        boolean vary;
        do {
            vary = false;

            // 1. 化简Nop
            simplifyNop();

            // 2. 化简Label
            simplifyLabel();

            // 3. 化简Exp
            simplifyExp();

            // 4. 划分函数区域
            convertToFuncBlock();

            // 5. 遍历函数块
            for (FuncBlock funcBlock : funcBlocks) {
                vary = vary | funcBlock.simplify();
            }

            // 6. 如果已经稳定，遍历函数块化简循环
            if (!vary) {
                for (FuncBlock funcBlock : funcBlocks) {
                    vary = vary | funcBlock.simplifyLoop();
                }
            }

        } while(vary);

        // 2. 清除中间代码列表
        midCodes.clear();

        // 3. 重新导出中间代码
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            midCodes.add(midCode);
        }

        // 4. 生成活跃变量信息
        for(FuncBlock funcBlock : funcBlocks) {
            funcBlock.extractToLiveOutUnitTable();
        }
    }

    // 1.1 化简Nop
    public void simplifyNop() {
        // 1. 不断获取中间代码，删除Nop()
        for(MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            if(midCode instanceof Nop) {
                midCode.removeFromMidCodeList();
            }
        }
    }

    // 1.2 化简Label
    public void simplifyLabel() {
        // 3.1 记录使用的标签
        HashSet<Label> usedLabels = new HashSet<>();

        // 3.2 遍历所有中间代码
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {

            // 3.3 如果是跳转
            if (midCode instanceof Jump) {
                // 3.4 获取跳转
                Jump jump = (Jump) midCode;

                // 3.5 获取跳转目的标签
                Label target = jump.getLabel().getTarget();

                // 3.6 如果本来下一个就是跳转目标，就删除这个跳转，自然进入下一个中间代码即可
                if (midCode.getNext() == target.getMidCode()) {
                    midCode.removeFromMidCodeList();
                }

                // 3.7 否则Jump的跳转目的标签不变，记录跳转目的的标签使用过
                else {
                    jump.setLabel(target);
                    usedLabels.add(target);
                }
            }

            // 3.3 如果是分支
            else if (midCode instanceof Branch){

                // 3.4 获取分支
                Branch branch = (Branch) midCode;

                // 3.5 获取分支跳转目的标签
                Label target = branch.getBranchLabel().getTarget();

                // 3.6 如果本来下一个就是跳转目标，就删除这个跳转，自然进入下一个中间代码即可
                if (midCode.getNext() == target.getMidCode()) {
                    midCode.removeFromMidCodeList();
                }

                // 3.7 否则获取分支的下一个中间代码
                else if (midCode.getNext() != null) {

                    // 3.8 获取分支的下一个中间代码
                    MidCode nextCode = midCode.getNext();

                    // 3.9 如果是跳转，并且跳转的下一个中间代码是分支跳转目标的中间代码，那么无论这个分支跳转成不成功，都会进入这个分支跳转的目的标签
                    // 3.9 不懂
                    if (nextCode instanceof Jump
                            && nextCode.getNext() == branch.getBranchLabel().getMidCode()
                            && LabelTable.getInstance().getLabelList(nextCode).isEmpty()) {
                        ((Branch) midCode).changeBranchOp(((Jump) nextCode).getLabel());
                        usedLabels.add(((Jump) nextCode).getLabel());
                        nextCode.removeFromMidCodeList();
                    }

                    // 3.9 不懂
                    else {
                        branch.setLabel(target);
                        usedLabels.add(target);
                    }
                }

                // 3.9 不懂
                else {
                    branch.setLabel(target);
                    usedLabels.add(target);
                }
            }
        }
    }

    // 1.3 化简Exp
    public void simplifyExp() {
        // 3.3 化简:Assign, Branch, Move
        for (MidCode midCode = head.getNext(); midCode != null; midCode = midCode.getNext()) {
            if (midCode instanceof Assign) {
                ((Assign) midCode).simplify();
            } else if (midCode instanceof Branch) {
                ((Branch) midCode).simplify();
            } else if (midCode instanceof Move) {
                ((Move) midCode).simplify();
            }
        }
    }

    // 2. 获取函数块列表
    public LinkedList<FuncBlock> getFuncBlocks() {
        return funcBlocks;
    }

    // 3. 划分函数块
    public void convertToFuncBlock() {
        // 3.1 遍历中间代码
        MidCode tempHead = this.head.getNext();
        MidCode tempTail = tempHead;

        // 3.2 清除函数块
        funcBlocks.clear();

        // 3.3 创建函数块
        while (tempTail.getNext() != null) {
            if (tempTail.getNext() instanceof FuncEntry) {
                funcBlocks.add(new FuncBlock(tempHead, tempTail));
                tempHead = tempTail.getNext();
            }
            tempTail = tempTail.getNext();
        }

        // 3.4 最后一个也要作为函数块
        funcBlocks.add(new FuncBlock(tempHead, tempTail));
    }
}




















