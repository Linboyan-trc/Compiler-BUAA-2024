package frontend.SyntaxTree.StmtNode;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTree.BlockItemNode;
import frontend.SyntaxTree.DeclNode;
import frontend.SyntaxTree.ExpNode.ExpNode;
import midend.LabelTable.Label;
import midend.MidCode.MidCode.*;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;

public class LoopNode implements StmtNode {
    private final SymbolTable symbolTable;
    private final ExpNode cond;
    private final StmtNode loopStmt;

    public LoopNode(SymbolTable symbolTable,
                    ExpNode loopCond,
                    StmtNode loopStmt) {
        this.symbolTable = symbolTable;
        this.cond = loopCond;
        this.loopStmt = loopStmt;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public LoopNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(BlockItemNode declNode, AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 创建标签
        Label loopBeginLabel = new Label();
        Label loopEndLabel = new Label();

        // 2. 设置标签
        MidCodeTable.getInstance().setLoop(loopBeginLabel, loopEndLabel);


        Nop loopBegin = new Nop();


        Value condValue = cond.generateMidCode();
        new Branch(Branch.BranchOp.EQ, condValue, new Imm(0), loopEndLabel);


        loopStmt.generateMidCode();


        new Jump(loopBeginLabel);


        Nop loopEnd = new Nop();


        loopBeginLabel.setMidCode(loopBegin);
        loopEndLabel.setMidCode(loopEnd);


        MidCodeTable.getInstance().unsetLoop();


        return null;
    }
}
