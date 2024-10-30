package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class UnaryExpNode implements ExpNode {
    // 1. <UnaryOP> + <UnaryExp>
    private Pair unaryOp;
    private ExpNode expNode;

    // 2.
    public UnaryExpNode() { }

    public void setUnaryOp(Pair unaryOp) {
        this.unaryOp = unaryOp;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 节点自带类型
        return expNode.getSyntaxType(symbolTable);
    }
}
