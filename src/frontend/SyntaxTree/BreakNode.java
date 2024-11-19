package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;

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
}
