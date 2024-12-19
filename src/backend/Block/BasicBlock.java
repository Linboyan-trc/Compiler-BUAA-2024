package backend.Block;

import midend.MidCode.MidCode.*;
import midend.MidCode.Optimize.*;
import midend.MidCode.Value.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringJoiner;

public class BasicBlock {
    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 对应的函数块，基础块起始和结束的中间代码
    private FuncBlock funcBlock;
    private MidCode head;
    private MidCode tail;
    // 2. 前基本块列表，后基本块列表
    private final HashSet<BasicBlock> previousBasicBlocks = new HashSet<>();
    private final HashSet<BasicBlock> nextBasicBlocks = new HashSet<>();

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 定义表，使用表
    private final HashSet<Value> defUnitTable = new HashSet<>();
    private final HashSet<Value> useUnitTable = new HashSet<>();

    // 2. 生成表，覆盖表
    private final HashSet<MidCode> genMidCodeTable = new HashSet<>();
    private final HashSet<MidCode> killMidCodeTable = new HashSet<>();

    // 3. 到达定义表
    private final HashSet<MidCode> reachInMidCodeTable = new HashSet<>();
    private final HashSet<MidCode> reachOutMidCodeTable = new HashSet<>();

    // 4. 活跃变量表
    private final HashSet<Value> liveInUnitTable = new HashSet<>();
    private final HashSet<Value> liveOutUnitTable = new HashSet<>();

    // 5. 中间代码到活跃变量表的映射
    private final HashMap<MidCode, HashSet<Value>> midCodeToLiveOutUnitTable = new HashMap<>();

    // 6. 使用时间
    private final HashMap<Value, HashSet<MidCode>> useTime = new HashMap<>();

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 指定一个基础块对应的函数块，和这个基础块的起始和结束的中间代码
    public BasicBlock(FuncBlock funcBlock, MidCode head, MidCode tail) {
        this.funcBlock = funcBlock;
        this.head = head;
        this.tail = tail;
    }

    // 2. 获取开始中间代码
    public MidCode getHead() {
        return head;
    }

    // 3. 获取结束中间代码
    public MidCode getTail() {
        return tail;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 添加基本块的后基本块
    public void linkToNext(BasicBlock next) {
        this.nextBasicBlocks.add(next);
        next.previousBasicBlocks.add(this);
    }

    // 2. 获取前基本块列表
    public HashSet<BasicBlock> getPreviousBasicBlocks() {
        return previousBasicBlocks;
    }

    // 3. 获取后基本块列表
    public HashSet<BasicBlock> getNextBasicBlocks() {
        return nextBasicBlocks;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 清除寄存器
    public void flushAll() {
        // 1. 定义表，使用表
        defUnitTable.clear();
        useUnitTable.clear();

        // 2. 生成表，覆盖表
        genMidCodeTable.clear();
        killMidCodeTable.clear();

        // 3. 到达定义表
        reachInMidCodeTable.clear();
        reachOutMidCodeTable.clear();

        // 4. 活跃变量表
        liveInUnitTable.clear();
        liveOutUnitTable.clear();
    }

    // 1.1 生成定义使用表
    public void extractToDefUse() {
        // 1. 获取基本块起始中间代码
        MidCode midCode = head;

        // 2. 遍历基本块中间代码
        while (midCode != tail.getNext()) {
            // 3. 是UseUnit
            // 3. 遍历UseUnit，如果不在基本块定义表里，也不是Imm，就加入到基本块使用表
            if (midCode instanceof UseUnit) {
                for (Value value : ((UseUnit) midCode).getUseUnit()) {
                    if (!defUnitTable.contains(value) && !(value instanceof Imm)) {
                        useUnitTable.add(value);
                    }
                }
            }

            // 4. 是DefUnit
            // 4. 获取DefUnit，添加到函数块的定义表
            // 4. 如果不在使用表中，就加入基本块的定义表
            if (midCode instanceof DefUnit) {
                Value defUnit = ((DefUnit) midCode).getDefUnit();
                funcBlock.setDefUnit(defUnit, midCode);
                if (!useUnitTable.contains(defUnit)) {
                    defUnitTable.add(defUnit);
                }
            }

            // 5. 继续遍历基本块中间代码
            midCode = midCode.getNext();
        }
    }

    // 1.2 生成GenKill表
    public void extractToGenKill() {
        // 1. 获取基本块结束中间代码
        MidCode midCode = tail;

        // 2. 从后往前遍历基本块中间代码
        while (midCode != head.getPrevious()) {
            // 3. 如果是定义变量，并且不在覆盖变量中，就添加到生成变量
            // 3. 并且把函数块中所有这个定义变量的定义变量添加到覆盖变量中
            if (midCode instanceof DefUnit) {
                if (!killMidCodeTable.contains(midCode)) {
                    genMidCodeTable.add(midCode);
                }
                killMidCodeTable.addAll(
                    funcBlock.getDefUnitToMidCodes(
                        ((DefUnit) midCode).getDefUnit()
                    )
                );
            }
            midCode = midCode.getPrevious();
        }
    }

    // 1.3 生成到达定义表
    public boolean extractToReachInOut() {
        // 1. 拷贝一份到达定义表
        HashSet<MidCode> oldreachOutMidCodeTable = new HashSet<>(reachOutMidCodeTable);

        // 2. 拷贝生成变量
        reachOutMidCodeTable.addAll(genMidCodeTable);

        // 3. 前面的代码更新到达定义表
        for (BasicBlock basicBlock : previousBasicBlocks) {
            reachInMidCodeTable.addAll(basicBlock.reachOutMidCodeTable);
        }

        // 4. 遍历到达定义变量
        for (MidCode midCode : reachInMidCodeTable) {
            // 5. 变量没有被覆盖，就加入到到达定义表中
            if (!killMidCodeTable.contains(midCode)) {
                reachOutMidCodeTable.add(midCode);
            }
        }

        // 5. 判断到达定义表是否已经稳定
        return !oldreachOutMidCodeTable.equals(reachOutMidCodeTable);
    }

    // 1.4 传播优化
    public boolean deliveryOptimize() {
        // 1. 记录是否稳定
        boolean changed = false;

        // 2. 从基本块起始中间代码开始遍历
        MidCode midCode = head;

        // 3.
        HashMap<Value, MidCode> intraDef = new HashMap<>();

        // 4. 从基本块起始中间代码遍历中间代码
        while (midCode != tail.getNext()) {
            // 4.1 如果是使用变量
            if (midCode instanceof UseUnit) {
                // 4.1.1 获取使用变量
                LinkedList<Value> useUnits = ((UseUnit) midCode).getUseUnit();

                // 4.1.2 遍历每一个使用变量
                for (Value useUnit : useUnits) {
                    // 4.1.3 如果不是全局变量，也不是返回值
                    if (!useUnit.isGlobal() && !useUnit.isReturn()) {
                        // 4.1.4
                        if (intraDef.containsKey(useUnit)) {
                            MidCode defMidCode = intraDef.get(useUnit);
                            if (!useUnit.isRunTimeInvariant(defMidCode, midCode)) {
                                continue;
                            }
                            if (midCode instanceof Declare) {
                                if (defMidCode instanceof Move) {
                                    Value value = ((Move) defMidCode).getSourceValue();
                                    if (value instanceof Imm || (value instanceof Word && !value.isGlobal() && !value.isReturn())) {
                                        ((Declare) midCode).changeToAnotherUnit(useUnit, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            else if (midCode instanceof Move) {
                                if (defMidCode instanceof Declare) {
                                    Declare declare = (Declare) defMidCode;
                                    if (declare.getUseUnit().size() == 1 && declare.getValue() instanceof Word) {
                                        Value value = declare.getUseUnit().get(0);
                                        if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                            ((Move) midCode).changeToAnotherUnit(useUnit, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                else if (defMidCode instanceof Load) {
                                    Load load = (Load) defMidCode;
                                    Addr value = load.getSourceValue();
                                    if (!value.isGlobal() && !value.isReturn()) {
                                        Load newLoad = new Load(load.isTemp(), ((Move) midCode).getTargetValue(), value);
                                        midCode.changeToAnother(newLoad);
                                        return true;
                                    }
                                }
                                else if (defMidCode instanceof Move) {
                                    Move move = (Move) defMidCode;
                                    Value value = move.getUseUnit().get(0);
                                    if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                        ((Move) midCode).changeToAnotherUnit(useUnit, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            else if (defMidCode instanceof Copy && ((Copy) defMidCode).getSource().size() == 1) {
                                Value value = ((Copy) defMidCode).getSource().get(0);
                                if (defMidCode instanceof Declare && ((Declare) defMidCode).getDefUnit() instanceof Addr) {
                                    continue;
                                }
                                if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                    ((UseUnit) midCode).changeToAnotherUnit(useUnit, value);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        // 4.1.5 如果使用变量是Word
                        else if (useUnit instanceof Word) {
                            // 4.1.6 此块进入的变量表
                            LinkedList<MidCode> midCodeReachIns = new LinkedList<>();
                            // 4.1.7 遍历此块进入的变量表
                            // 4.1.7 如果是定义变量，并且定义变量和现在这个使用变量一样，就加入到新的进入变量表
                            for (MidCode item : reachInMidCodeTable) {
                                if (item instanceof DefUnit) {
                                    DefUnit defUnit = (DefUnit) item;
                                    if (defUnit.getDefUnit().equals(useUnit)) {
                                        midCodeReachIns.add(item);
                                    }
                                }
                            }
                            // 4.1.8 如果新的进入变量表只有一个变量
                            if (midCodeReachIns.size() == 1) {
                                // 4.1.9 获取这个进入时定义，现在在使用的变量
                                MidCode defMidCode = midCodeReachIns.get(0);
                                // 4.1.10 如果不是不变的，就遍历下一个使用变量
                                if (!isDataFlowInvariant(defMidCode, midCode)) {
                                    continue;
                                }
                                // 4.1.11 如果是声明
                                if (midCode instanceof Declare) {
                                    // 4.1.12 并且是Move
                                    if (defMidCode instanceof Move) {
                                        Value value = ((Move) defMidCode).getSourceValue();
                                        if (value instanceof Imm || (value instanceof Word && !value.isGlobal() && !value.isReturn())) {
                                            ((Declare) midCode).changeToAnotherUnit(useUnit, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                // 4.1.12 如果是Move
                                else if (midCode instanceof Move) {
                                    if (defMidCode instanceof Declare) {
                                        Declare declare = (Declare) defMidCode;
                                        if (declare.getUseUnit().size() == 1 && declare.getValue() instanceof Word) {
                                            Value value = declare.getUseUnit().get(0);
                                            if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                                ((Move) midCode).changeToAnotherUnit(useUnit, value);
                                                changed = true;
                                                break;
                                            }
                                        }
                                    } else if (defMidCode instanceof Load) {
                                        Load load = (Load) defMidCode;
                                        Addr value = load.getSourceValue();
                                        if (!value.isGlobal() && !value.isReturn()) {
                                            Load newLoad = new Load(load.isTemp(), ((Move) midCode).getTargetValue(), value);
                                            midCode.changeToAnother(newLoad);
                                            return true;
                                        }
                                    } else if (defMidCode instanceof Move) {
                                        Move move = (Move) defMidCode;
                                        Value value = move.getUseUnit().get(0);
                                        if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                            ((Move) midCode).changeToAnotherUnit(useUnit, value);
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                // 4.1.13 如果是Copy，并且只Copy一个值
                                else if (defMidCode instanceof Copy && ((Copy) defMidCode).getSource().size() == 1) {
                                    Value value = ((Copy) defMidCode).getSource().get(0);
                                    if (defMidCode instanceof Declare && ((Declare) defMidCode).getDefUnit() instanceof Addr) {
                                        continue;
                                    }
                                    if (value instanceof Imm || (!value.isGlobal() && !value.isReturn())) {
                                        ((UseUnit) midCode).changeToAnotherUnit(useUnit, value);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4.2 如果是定义变量
            if (midCode instanceof DefUnit) {
                Value defVal = ((DefUnit) midCode).getDefUnit();
                intraDef.put(defVal, midCode);
            }

            // 4.3 继续遍历
            midCode = midCode.getNext();
        }

        // 5. 返回是否稳定
        return changed;
    }

    // 1.5 活跃变量
    public boolean extractToLiveInOut() {
        // 1. 拷贝一份活跃变量表
        HashSet<Value> oldliveInUnitTable = new HashSet<>(liveInUnitTable);

        // 2. 拷贝使用变量
        liveInUnitTable.addAll(useUnitTable);

        // 3. 后面的代码更新活跃变量表
        for(BasicBlock basicBlock : nextBasicBlocks) {
            liveOutUnitTable.addAll(basicBlock.liveInUnitTable);
        }

        // 4. 遍历活跃变量
        for (Value value : liveOutUnitTable) {
            // 5. 活跃变量不是定义变量，就加入到活跃变量表中
            if (!defUnitTable.contains(value)) {
                liveInUnitTable.add(value);
            }
        }

        // 5. 判断活跃变量表是否已经稳定
        return !oldliveInUnitTable.equals(liveInUnitTable);
    }

    // 1.6 移除死代码
    public boolean diminishDeadCode() {
        // 1. 是否稳定
        boolean changed = false;

        // 2. 基本块不是函数块的开始，并且没有前置基本块，那么就是一段不会到达的基本块，需要移除
        if (!funcBlock.isFuncBlockStart(this) && previousBasicBlocks.isEmpty()) {
            MidCode midCode = head;
            while (midCode != tail.getNext()) {
                Nop nop = new Nop();
                midCode.changeToAnother(nop);
                if (midCode == head) {
                    head = nop;
                }
                if (midCode == tail) {
                    tail = nop;
                }
                midCode = nop.getNext();
            }
            return true;
        }

        // 3. 否则
        else {
            MidCode midCode = head;
            while (midCode != tail.getNext()) {
                if (midCode instanceof DefUnit) {
                    boolean used = false;
                    Value defVal = ((DefUnit) midCode).getDefUnit();
                    MidCode item = midCode.getNext();
                    while (item != tail.getNext()) {
                        if (item instanceof UseUnit && ((UseUnit) item).getUseUnit().contains(defVal)) {
                            used = true;
                            break;
                        }
                        item = item.getNext();
                    }
                    if (!used && !liveOutUnitTable.contains(defVal) && !defVal.isGlobal() && !defVal.isReturn()) {
                        midCode.changeToAnother(new Nop());
                        changed = true;
                    }
                }
                midCode = midCode.getNext();
            }
        }

        // 4. 返回是否稳定
        return changed;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 判断这个变量是否是不变的
    public boolean isDataFlowInvariant(MidCode defMidCode, MidCode midCode) {
        // 1. Load则是变化的
        if (defMidCode instanceof Load) {
            return false;
        }

        // 2. 使用变量
        LinkedList<Value> useUnits = new LinkedList<>();
        if (defMidCode instanceof UseUnit) {
            useUnits.addAll(((UseUnit) defMidCode).getUseUnit());
        }

        // 3. 基本块
        HashSet<BasicBlock> visited = new HashSet<>();
        for (BasicBlock basicBlock : funcBlock.getBasicBlocks()) {
            if (basicBlock.getMidCodes().contains(defMidCode)) {
                return DFS(basicBlock, useUnits, defMidCode, midCode, visited);
            }
        }
        return false;
    }

    // 2. 获取基本块中间代码
    public HashSet<MidCode> getMidCodes() {
        // 1. 中间代码
        HashSet<MidCode> midCodes = new HashSet<>();

        // 2. 从头遍历
        MidCode midCode = head;
        while (midCode != tail) {
            midCodes.add(midCode);
            midCode = midCode.getNext();
        }
        midCodes.add(tail);
        return midCodes;
    }

    // 3. 递归地检查程序的基本块中变量是否不变
    public boolean DFS(BasicBlock basicBlock,
                       LinkedList<Value> useUnits,
                       MidCode startCode,
                       MidCode endCode,
                       HashSet<BasicBlock> visited) {
        // 1. 起始中间代码
        MidCode midCode = startCode;

        // 2. 已经遍历过的基本块
        visited.add(basicBlock);

        // 3. 遍历中间代码
        while (midCode != endCode) {
            if (midCode instanceof DefUnit && useUnits.contains(((DefUnit) midCode).getDefUnit())) {
                return false;
            }
            if (midCode == basicBlock.tail) {
                break;
            }
            midCode = midCode.getNext();
        }
        if (midCode == endCode) {
            return true;
        }
        for (BasicBlock item : basicBlock.nextBasicBlocks) {
            if (!visited.contains(item)) {
                if (!DFS(item, useUnits, item.head, endCode, visited)) {
                    return false;
                }
            }
        }
        return true;
    }

    // 4. 获得使用变量
    public HashSet<Value> getUseUnitTable() {
        return useUnitTable;
    }

    // 5. 获得到达变量
    public HashSet<MidCode> getReachInMidCodeTable() {
        return reachInMidCodeTable;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成基本块活跃变量
    public void extractToLiveOutUnitTable() {
        // 1. 遍历基本块的中间代码
        MidCode tempCode = head;
        while (tempCode != tail.getNext()) {
            // 2. 拷贝活跃变量表
            HashSet<Value> liveTable = new HashSet<>(liveOutUnitTable);
            // 3. 从后往前遍历
            MidCode midCode = tail;
            while (midCode != tempCode) {
                if (midCode instanceof DefUnit) {
                    Value defUnit = ((DefUnit) midCode).getDefUnit();
                    liveTable.remove(defUnit);
                }
                if (midCode instanceof UseUnit) {
                    for (Value useUnit : ((UseUnit) midCode).getUseUnit()) {
                        if (!(useUnit instanceof Imm)) {
                            liveTable.add(useUnit);
                        }
                    }
                }
                midCode = midCode.getPrevious();
            }
            // 4. 建立中间代码到活跃变量表的映射
            midCodeToLiveOutUnitTable.put(tempCode, liveTable);
            // 5. 继续遍历
            tempCode = tempCode.getNext();
        }
    }

    // 2. 计算变量的使用时间
    public void calculateUseTime() {
        MidCode midCode = head;
        while (midCode != tail.getNext()) {
            if (midCode instanceof UseUnit) {
                for (Value useVal : ((UseUnit) midCode).getUseUnit()) {
                    if (!useVal.isGlobal() && !useVal.isReturn()) {
                        if (!useTime.containsKey(useVal)) {
                            useTime.put(useVal, new HashSet<>());
                        }
                        useTime.get(useVal).add(midCode);
                    }
                }
            }
            midCode = midCode.getNext();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 获得活跃变量
    public HashSet<Value> getliveOutUnitTable() {
        return liveOutUnitTable;
    }

    public boolean isLive(Value value, MidCode code) {
        return midCodeToLiveOutUnitTable.get(code).contains(value);
    }

    public boolean usedUp(Value useVal, MidCode midCode) {
        if (!useTime.containsKey(useVal)) {
            return false;
        } else {
            HashSet<MidCode> useSet = useTime.get(useVal);
            useSet.remove(midCode);
            return useSet.isEmpty();
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        MidCode midCode = head;

        while (midCode != tail.getNext()) {
            if (midCode != null) {
                result.append(midCode).append("\n");
            } else {
                result.append("null\n");
                break;
            }
            midCode = midCode.getNext();
        }

        return result.toString();
    }
}
