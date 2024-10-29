package SyntaxTree;

public class ForNode implements StmtNode {
    // 1. 针对for <ForStmt> <Cond> <ForStmt> <Stmt>
    private ForStmtNode forStmtNodeFirst = null;
    private ExpNode cond;
    private ForStmtNode forStmtNodeSecond = null;
    private StmtNode stmt;

    // 2.
    public ForNode() { }

    public void setForStmtNodeFirst(ForStmtNode forStmtNodeFirst) {
        this.forStmtNodeFirst = forStmtNodeFirst;
    }

    public void setCond(ExpNode cond) {
        this.cond = cond;
    }

    public void setForStmtNodeSecond(ForStmtNode forStmtNodeSecond) {
        this.forStmtNodeSecond = forStmtNodeSecond;
    }

    public void setStmt(StmtNode stmt) {
        this.stmt = stmt;
    }

    // 3. 检查
    // TODO: 错误处理
}
