package SyntaxTree;

public class ForStmtNode {
    // 1. <ForStmt> = <LVal> '=' <Exp>
    private LValNode lValNode;
    private ExpNode expNode;

    // 2.
    public ForStmtNode() { }

    public void setlValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    // 3. 检查
    // TODO: 错误处理
}
