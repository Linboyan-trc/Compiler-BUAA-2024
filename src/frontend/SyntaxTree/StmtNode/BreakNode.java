package frontend.SyntaxTree.StmtNode;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTree.BlockItemNode;
import frontend.SyntaxTree.DeclNode;
import midend.MidCode.MidCode.Jump;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class BreakNode implements StmtNode {
    private final SymbolTable symbolTable;

    public BreakNode(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public BreakNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(BlockItemNode declNode, AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        new Jump(MidCodeTable.getInstance().getLoopEnd());
        return null;
    }
}
