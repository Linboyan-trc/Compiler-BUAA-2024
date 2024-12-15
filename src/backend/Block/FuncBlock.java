package backend.Block;

import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;
import midend.MidCode.MidCode.*;
import midend.MidCode.MidCode.MidCode;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class FuncBlock {
    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 函数块的起始和结束中间代码
    private MidCode head;
    private MidCode tail;
    // 2. 基本块列表
    private final LinkedList<BasicBlock> basicBlocks = new LinkedList<>();
    // 3. 循环块列表
    private final LinkedList<LoopBlock> loopBlocks = new LinkedList<>();

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 定义变量和中间代码的对应
    private final HashMap<Value, LinkedList<MidCode>> defUnitToMidCodes = new HashMap<>();

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构建一个函数块，然后划分成基础块
    public FuncBlock(MidCode head, MidCode tail) {
        this.head = head;
        this.tail = tail;
        convertToBasicBlock();
    }

    // 2. 划分并且连接基本块
    public void convertToBasicBlock() {
        // 1. 从起始中间代码遍历到结束中间代码
        MidCode tempHead = head;
        MidCode tempTail = head;
        while (tempTail != tail) {
            // 1.1 当前中间代码有对应的符号表
            // 1.1 或者现在的中间代码是Jump或者Branch或者Return
            if (!LabelTable.getInstance().getLabelList(tempTail.getNext()).isEmpty()
                    || tempTail instanceof Jump || tempTail instanceof Branch || tempTail instanceof Return) {
                // 1.2 就创建一个新的基本块
                // 1.2 加入到基本块列表
                basicBlocks.add(new BasicBlock(this, tempHead, tempTail));

                // 1.3 更新遍历头
                tempHead = tempTail.getNext();
            }

            // 1.4 更新遍历尾
            tempTail = tempTail.getNext();
        }

        // 2. 最后一句结束作为基本块加入
        basicBlocks.add(new BasicBlock(this, tempHead, tempTail));

        // 3. 遍历基本块
        for (int index = 0; index < basicBlocks.size(); index++) {
            // 3.1 基本块结尾是Jump
            if (basicBlocks.get(index).getTail() instanceof Jump) {
                // 3.1 获取Jump
                Jump jump = (Jump) basicBlocks.get(index).getTail();

                // 3.2 获取Jump目的标签的中间代码
                MidCode midCode = jump.getLabel().getMidCode();

                // 3.3 如果在此函数的所有基本块中存在开始中间代码是，当前基本块Jump目的标签的中间代码，就连接两个基本块
                for (BasicBlock basicBlock : basicBlocks) {
                    if (basicBlock.getHead() == midCode) {
                        basicBlocks.get(index).linkToNext(basicBlock);
                        break;
                    }
                }
            }

            // 3.1 基本块结尾是Branch
            else if (basicBlocks.get(index).getTail() instanceof Branch) {
                // 3.1 获取Branch
                Branch branch = (Branch) basicBlocks.get(index).getTail();

                // 3.2 获取Branch目的标签的中间代码
                MidCode midCode = branch.getBranchLabel().getMidCode();

                // 3.3 如果在此函数的所有基本块中存在开始中间代码是，当前基本块Jump目的标签的中间代码，就连接两个基本块
                for (BasicBlock basicBlock : basicBlocks) {
                    if (basicBlock.getHead() == midCode) {
                        basicBlocks.get(index).linkToNext(basicBlock);
                        break;
                    }
                }

                // 3.4 对于Branch还要连接本身在此基本块列表中的下一个基本块
                if (index != basicBlocks.size() - 1) {
                    basicBlocks.get(index).linkToNext(basicBlocks.get(index + 1));
                }
            }

            // 3.1 基本块结尾补是Return，且不是最后一个基本块，也就进行基本块之间的连接
            else if (!(basicBlocks.get(index).getTail() instanceof Return)
                    && index != basicBlocks.size() - 1) {
                basicBlocks.get(index).linkToNext(basicBlocks.get(index + 1));
            }
        }
    }

    // 3. 获取基本块列表
    public LinkedList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    public boolean simplify() {
        // 1.1 对每个基本块，flushAll
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.flushAll();
        }

        // 1.2 生成定义使用表
        extractToDefUse();

        // 1.3 生成GenKill表
        extractToGenKill();

        // 1.4 生成到达定义表
        extractToReachInOut();

        // 1.5 传播优化
        // 1.5 如果传播优化了先生成中间代码，等待下一轮的优化
        if (deliveryOptimize()) {
            return true;
        }

        // 1.6 活跃变量
        extractToLiveInOut();

        // 1.7 移除死代码
        return diminishDeadCode();
    }

    // 1.1 生成定义使用表
    public void extractToDefUse() {
        // 1. 每个基本块生成定义使用表
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.extractToDefUse();
        }
    }

    // 1.2 生成GenKill表
    public void extractToGenKill() {
        // 1. 每个基本块生成GenKill表
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.extractToGenKill();
        }
    }

    // 1.3 生成到达定义表
    public void extractToReachInOut() {
        // 1. 每个基本块生成到达定义表，直到达定义表稳定
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlocks) {
                changed |= basicBlock.extractToReachInOut();
            }
        } while (changed);
    }

    // 1.4 传播优化
    public boolean deliveryOptimize() {
        // 1. 每个基本块传播优化，直到传播优化稳定
        boolean changed = false;
        for (BasicBlock basicBlock : basicBlocks) {
            changed |= basicBlock.deliveryOptimize();
        }
        return changed;
    }

    // 1.5 活跃变量
    public void extractToLiveInOut() {
        // 1. 每个基本块活跃变量，直到活跃变量稳定
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlocks) {
                changed |= basicBlock.extractToLiveInOut();
            }
        } while (changed);
    }

    // 1.6 移除死代码
    public boolean diminishDeadCode() {
        // 1. 每个基本块活跃变量，直到移除死代码稳定
        boolean changed = false;
        for (BasicBlock basicBlock : basicBlocks) {
            changed |= basicBlock.diminishDeadCode();
        }
        return changed;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 添加到函数块的定义变量
    public void setDefUnit(Value value, MidCode midCode) {
        if (!defUnitToMidCodes.containsKey(value)) {
            defUnitToMidCodes.put(value, new LinkedList<>());
        }
        defUnitToMidCodes.get(value).add(midCode);
    }

    // 2. 获取一个定义变量在函数块中对应的中间代码
    public LinkedList<MidCode> getDefUnitToMidCodes(Value defVal) {
        LinkedList<MidCode> midCodes = defUnitToMidCodes.containsKey(defVal) ? defUnitToMidCodes.get(defVal) : new LinkedList<>();
        return midCodes;
    }

    // 3. 判断一个基本块是否是函数块的开始
    public boolean isFuncBlockStart(BasicBlock basicBlock) {
        return basicBlocks.getFirst() == basicBlock;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 简化循环
    public boolean simplifyLoop() {
        convertToLoopBlock();
        return extractLoopBlockInvariant();
    }

    // 2. 划分出循环块
    public void convertToLoopBlock() {
        // 1. 获取循环标签
        HashSet<Label> loopMark = MidCodeTable.getInstance().getLoopMark();

        // 2. 遍历基本块
        for (int i = 0; i < basicBlocks.size(); i++) {
            // 2.1 获取基本块
            BasicBlock basicBlock = basicBlocks.get(i);
            // 2.2 获取基本块头对应的标签
            LinkedList<Label> labelList = LabelTable.getInstance().getLabelList(basicBlock.getHead());
            // 2.3 遍历标签
            for (Label label : labelList) {
                // 2.4 如果循环标签中含有基本块头对应的标签中的标签
                if (loopMark.contains(label)) {
                    // 2.4.1 新的基本块列表
                    LinkedList<BasicBlock> loopBasicBlocks = new LinkedList<>();
                    // 2.4.2 当前基本块加入到新的基本块列表
                    loopBasicBlocks.add(basicBlock);
                    // 2.4.3 从后往前遍历基本块列表
                    int j = basicBlocks.size() - 1;
                    for (; j >= i; j--) {
                        // 2.4.3.1 获取后面的基本块
                        BasicBlock tempBasicBlock = basicBlocks.get(j);
                        // 2.4.3.2 基本块结尾是Branch
                        if (tempBasicBlock.getTail() instanceof Branch) {
                            Branch branch = (Branch) tempBasicBlock.getTail();
                            if (branch.getBranchLabel().getMidCode() == basicBlock.getHead()) {
                                break;
                            }
                        }
                        // 2.4.3.3 基本块结尾是Jump
                        else if (tempBasicBlock.getTail() instanceof Jump) {
                            Jump jump = (Jump) tempBasicBlock.getTail();
                            if (jump.getLabel().getMidCode() == basicBlock.getHead()) {
                                break;
                            }
                        }
                    }
                    // 2.4.4 再从前往后遍历一次，加入新的基本块列表
                    for (int k = i + 1; k <= j; k++) {
                        loopBasicBlocks.add(basicBlocks.get(k));
                    }
                    // 2.4.5 加入循环块列表
                    loopBlocks.add(new LoopBlock(loopBasicBlocks));
                }
            }
        }
    }

    // 3. 循环块生成循环不变量
    public boolean extractLoopBlockInvariant() {
        // 1. 遍历循环块
        for (LoopBlock loopBlock : loopBlocks) {
            // 2. 每个循环块生成循环不变量
            if (loopBlock.extractLoopBlockInvariant()) {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成函数块活跃变量
    public void extractToLiveOutUnitTable() {
        for(BasicBlock basicBlock : basicBlocks) {
            basicBlock.extractToLiveOutUnitTable();
        }
    }


}
