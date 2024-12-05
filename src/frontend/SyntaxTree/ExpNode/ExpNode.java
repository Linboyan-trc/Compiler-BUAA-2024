package frontend.SyntaxTree.ExpNode;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.StmtNode.StmtNode;

public interface ExpNode extends StmtNode {
    public SyntaxType getSyntaxType(SymbolTable symbolTable);

    ExpNode simplify();
}
