package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public interface ExpNode extends StmtNode {
    public SyntaxType getSyntaxType(SymbolTable symbolTable);
}
