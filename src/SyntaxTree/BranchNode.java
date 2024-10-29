package SyntaxTree;

public class BranchNode implements StmtNode {
    // 1. 针对if <Cond> <Stmt> else <Stmt>
    private ExpNode cond;
    private StmtNode ifStmt;
    public StmtNode elseStmt = null;

    // 2. 构造 + 设置<Cond> + 设置<Stmt> + 设置<Stmt>
    public BranchNode() { }

    public void setCond(ExpNode cond) {
        this.cond = cond;
    }

    public void setIfStmt(StmtNode ifStmt) {
        this.ifStmt = ifStmt;
    }

    public void setElseStmt(StmtNode elseStmt) {
        this.elseStmt = elseStmt;
    }

    // 3. 检查
    // TODO: 错误处理
}
