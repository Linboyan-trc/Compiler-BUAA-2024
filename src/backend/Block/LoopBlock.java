package backend.Block;

import midend.MidCode.MidCode.*;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LoopBlock {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 基本块列表
    private LinkedList<BasicBlock> basicBlocks;
    // 2. 出口块列表
    private HashSet<BasicBlock> exitBasicBlocks = new HashSet<>();
    // 3.
    private HashMap<BasicBlock, HashSet<BasicBlock>> basicBlockToBasicBlockTable = new HashMap<>();
    private HashSet<BasicBlock> mainBlocks = new HashSet<>();
    // 4. 不变量
    private LinkedList<MidCode> invariants = new LinkedList<>();
    // 5.
    private HashMap<Value, HashSet<MidCode>> defUnitToMidCodeTable = new HashMap<>();
    // 6.
    private BasicBlock entryBlock = new BasicBlock(null, null, null);

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构造一个循环块
    public LoopBlock(LinkedList<BasicBlock> basicBlocks) {
        // 1. 循环块列表
        this.basicBlocks = basicBlocks;

        // 2. 出口块列表
        for (BasicBlock basicBlock : basicBlocks) {
            for (BasicBlock item : basicBlock.getNextBasicBlocks()) {
                if (!basicBlocks.contains(item)) {
                    exitBasicBlocks.add(basicBlock);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 循环块生成循环不变量
    public boolean extractLoopBlockInvariant() {
        // 1. 更新outMap，为基本块到哈希表的映射
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlockToBasicBlockTable.put(basicBlock, new HashSet<>());
        }

        // 2.
        generateNodeInOut();

        // 3. 更新mainBlocks
        for (BasicBlock basicBlock :exitBasicBlocks) {
            mainBlocks.addAll(basicBlockToBasicBlockTable.get(basicBlock));
        }

        // 4. 更新mainBlocks
        for (BasicBlock basicBlock :exitBasicBlocks) {
            mainBlocks.removeIf(item -> !basicBlockToBasicBlockTable.get(basicBlock).contains(item));
        }

        // 5.
        generateDefMap();

        // 6.
        generateInvariant();

        // 7.
        if (!invariants.isEmpty()) {
            Nop nop = new Nop();
            MidCode previous = basicBlocks.get(0).getHead().getPrevious();
            previous.linkToNext(nop).linkToNext(basicBlocks.get(0).getHead());
            for (BasicBlock basicBlock : basicBlocks) {
                for (MidCode midCode = basicBlock.getHead();
                     midCode != basicBlock.getTail().getNext();
                     midCode = midCode.getNext()) {
                    if (invariants.contains(midCode)) {
                        midCode.removeFromMidCodeList();
                        previous = previous.linkToNext(midCode);
                    }
                }
            }
            previous.linkToNext(nop);
        }

        // 8.
        return !invariants.isEmpty();
    }

    private void generateNodeInOut() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlocks) {
                HashSet<BasicBlock> oldOut = new HashSet<>(basicBlockToBasicBlockTable.get(basicBlock));
                HashSet<BasicBlock> out = basicBlockToBasicBlockTable.get(basicBlock);
                HashSet<BasicBlock> in = new HashSet<>();
                HashSet<BasicBlock> prev = basicBlock.getPreviousBasicBlocks();
                if (prev.size() != 0) {
                    prev.forEach(item -> in.addAll(getOutNode(item)));
                    prev.forEach(item -> in.removeIf(block -> !getOutNode(item).contains(block)));
                }
                out.addAll(in);
                out.add(basicBlock);
                changed |= !oldOut.equals(out);
            }
        } while (changed);
    }

    public void generateDefMap() {
        for (BasicBlock basicBlock : basicBlocks) {
            MidCode midCode = basicBlock.getHead();
            while (midCode != basicBlock.getTail().getNext()) {
                if (midCode instanceof DefUnit) {
                    Value value = ((DefUnit) midCode).getDefUnit();
                    if (!defUnitToMidCodeTable.containsKey(value)) {
                        defUnitToMidCodeTable.put(value, new HashSet<>());
                    }
                    defUnitToMidCodeTable.get(value).add(midCode);
                }
                midCode = midCode.getNext();
            }
        }
    }

    public void generateInvariant() {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : mainBlocks) {
                MidCode midCode = basicBlock.getHead();
                while (midCode != basicBlock.getTail().getNext()) {
                    if (midCode instanceof Assign || midCode instanceof Declare ||
                            midCode instanceof Move || midCode instanceof Store) {
                        if (!invariants.contains(midCode) && isInvariant(midCode)) {
                            invariants.add(midCode);
                            changed = true;
                        }
                    }
                    midCode = midCode.getNext();
                }
            }
        } while (changed);
    }

    private HashSet<BasicBlock> getOutNode(BasicBlock basicBlock) {
        if (basicBlockToBasicBlockTable.containsKey(basicBlock)) {
            return basicBlockToBasicBlockTable.get(basicBlock);
        } else {
            HashSet<BasicBlock> outNode = new HashSet<>();
            outNode.add(entryBlock);
            return outNode;
        }
    }

    public boolean isInvariant(MidCode midCode) {
        if (midCode instanceof DefUnit) {
            if (defUnitToMidCodeTable.getOrDefault(((DefUnit) midCode).getDefUnit(), new HashSet<>()).size() > 1) {
                return false;
            }
            for (BasicBlock basicBlock : basicBlocks) {
                HashSet<MidCode> reachIn = basicBlock.getReachInMidCodeTable();
                for (MidCode code : reachIn) {
                    if (code != midCode && code instanceof DefUnit &&
                            ((DefUnit) code).getDefUnit().equals(((DefUnit) midCode).getDefUnit()) &&
                            basicBlock.getUseUnitTable().contains(((DefUnit) midCode).getDefUnit())) {
                        return false;
                    }
                }
            }
        }
        LinkedList<Value> usedValue = ((UseUnit) midCode).getUseUnit();
        for (Value value : usedValue) {
            if (value.isGlobal() || value.isReturn()) {
                return false;
            } else if (!(value instanceof Imm)) {
                HashSet<MidCode> totDef = new HashSet<>(defUnitToMidCodeTable.getOrDefault(value, new HashSet<>()));
                if (totDef.size() > 1) {
                    return false;
                } else {
                    totDef.removeIf(invariants::contains);
                    if (!totDef.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public BasicBlock getLoopBlockBeginBasicBlock() {
        return basicBlocks.getFirst();
    }

    public BasicBlock getLoopBlockEndBasicBlock() {
        return basicBlocks.getLast();
    }
}
