package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
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
    public Value generateMidCode() {
        new Jump(MidCodeTable.getInstance().getLoopEnd());
        return null;
    }
}
