package SyntaxTree;

public class GetIntNode implements StmtNode {
    // 1. <LVal>
    private LValNode lValNode;

    // 2. 构造
    public GetIntNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    // 3. 检查
    // TODO: 错误处理
}
