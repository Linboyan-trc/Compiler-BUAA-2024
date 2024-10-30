package SyntaxTree;

import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public interface ExpNode extends StmtNode {
    public SyntaxType getSyntaxType(SymbolTable symbolTable);
}
