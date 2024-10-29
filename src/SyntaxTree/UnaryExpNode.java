package SyntaxTree;

import Lexer.Pair;

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
}
