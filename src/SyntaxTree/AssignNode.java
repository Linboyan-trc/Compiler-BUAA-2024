package SyntaxTree;

public class AssignNode implements StmtNode {
    // 1. <LValNode> + <ExpNode>
    private LValNode lValNode;
    private ExpNode expNode;

    // 2.
    public AssignNode() { }

    public void setLValNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public void setExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    // 3. 检查
    // TODO: 错误处理
}
