package frontend.SyntaxTree.StmtNode;

import frontend.SyntaxTable.SymbolTable;
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
    public boolean hasContinue(AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 创建标签
        Label loopBeginLabel = new Label();
        Label loopEndLabel = new Label();
        Label stmtBeginLabel = new Label();

        // 2. 设置标签
        MidCodeTable.getInstance().setLoop(loopBeginLabel, loopEndLabel);
        MidCodeTable.getInstance().markLoop(stmtBeginLabel);


        Nop loopBegin = new Nop();
        MidCodeTable.getInstance().addToMidCodes(loopBegin);

        // 4. 循环体
        StmtNode beginBranch = new BranchNode(symbolTable, cond, new NopNode(), new JumpNode(loopEndLabel)).simplify();
        beginBranch.generateMidCode();


        Nop stmtBegin = new Nop();
        MidCodeTable.getInstance().addToMidCodes(stmtBegin);


        loopStmt.generateMidCode();


        StmtNode endBranch = new BranchNode(symbolTable, cond, new JumpNode(stmtBeginLabel), null).simplify();
        endBranch.generateMidCode();


        Nop loopEnd = new Nop();
        MidCodeTable.getInstance().addToMidCodes(loopEnd);


        loopBeginLabel.setMidCode(loopBegin);
        loopEndLabel.setMidCode(loopEnd);


        stmtBeginLabel.setMidCode(stmtBegin);
        MidCodeTable.getInstance().unsetLoop();


        return null;
    }
}























