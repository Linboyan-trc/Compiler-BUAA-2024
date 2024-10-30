package SyntaxTree;

import Lexer.Pair;

public class ReturnNode implements StmtNode {
    // 1. Pair + <Exp>
    private Pair pair;
    private ExpNode expNode = null;

    // 2.
    public ReturnNode(Pair pair) {
        this.pair = pair;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    // 1. 获取return行号，以及是否具有返回值
    public int getLineNumber() {
        return pair.getLineNumber();
    }

    public boolean hasExpNode() {
        return expNode != null;
    }
}
