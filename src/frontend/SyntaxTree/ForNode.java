package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public class ForNode implements StmtNode {
    // 1. 针对for <ForStmt> <Cond> <ForStmt> <Stmt>
    private final SymbolTable symbolTable;
    private ForStmtNode forStmtNodeFirst = null;
    private ExpNode cond;
    private ForStmtNode forStmtNodeSecond = null;
    private StmtNode stmt;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2.
    public ForNode(SymbolTable symbolTable,
                   ForStmtNode forStmtNodeFirst, ExpNode cond, ForStmtNode forStmtNodeSecond,
                   StmtNode stmt) {
        this.symbolTable = symbolTable;
        this.forStmtNodeFirst = forStmtNodeFirst;
        this.cond = cond;
        this.forStmtNodeSecond = forStmtNodeSecond;
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
