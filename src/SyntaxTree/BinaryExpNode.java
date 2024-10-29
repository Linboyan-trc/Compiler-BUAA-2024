package SyntaxTree;

import Lexer.Pair;

public class BinaryExpNode implements ExpNode {
    // 1. 就是()*(), ()+()
    private ExpNode leftExp;
    private Pair binaryOp;
    private ExpNode rightExp;

    // 2.
    public BinaryExpNode() { }

    public void setBinaryOp(Pair binaryOp) {
        this.binaryOp = binaryOp;
    }

    public void setLeftExp(ExpNode expNode) {
        this.leftExp = expNode;
    }

    public void setRightExp(ExpNode expNode) {
        this.rightExp = expNode;
    }
}
