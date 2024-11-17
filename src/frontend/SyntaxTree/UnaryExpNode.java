package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public class UnaryExpNode implements ExpNode {
    // 1. <UnaryOP> + <UnaryExp>
    private final SymbolTable symbolTable;
    private Pair unaryOp;
    private ExpNode expNode;

    // 2.
    public UnaryExpNode(SymbolTable symbolTable, Pair unaryOp, ExpNode expNode) {
        this.symbolTable = symbolTable;
        this.unaryOp = unaryOp;
        this.expNode = expNode;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 节点自带类型
        return expNode.getSyntaxType(symbolTable);
    }
}
