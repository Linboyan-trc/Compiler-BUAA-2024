package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;

public class ContinueNode implements StmtNode {
    private final SymbolTable symbolTable;

    public ContinueNode(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
}
