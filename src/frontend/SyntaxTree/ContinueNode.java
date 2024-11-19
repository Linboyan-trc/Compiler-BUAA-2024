package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;

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

}
