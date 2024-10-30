package SyntaxTree;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class ForNode implements StmtNode {
    // 1. 针对for <ForStmt> <Cond> <ForStmt> <Stmt>
    private ForStmtNode forStmtNodeFirst = null;
    private ExpNode cond;
    private ForStmtNode forStmtNodeSecond = null;
    private StmtNode stmt;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

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
    // 3. 专用于对VoidFunc的检查
    public void checkForError(SyntaxType funcDefSyntaxType) {
        if(stmt instanceof BlockNode) {
            ((BlockNode) stmt).checkForError(funcDefSyntaxType);
        } else if (stmt instanceof ReturnNode && ((ReturnNode) stmt).hasExpNode()) {
            errorHandler.addError(new ErrorRecord(((ReturnNode) stmt).getLineNumber(), 'f'));
        }
    }
}
