package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
import midend.MidCode.MidCode.Jump;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class ContinueNode implements StmtNode {
    private final SymbolTable symbolTable;

    public ContinueNode(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ContinueNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode){
        return true;
    }

    @Override
    public Value generateMidCode() {
        new Jump(MidCodeTable.getInstance().getLoopBegin());
        return null;
    }
}
