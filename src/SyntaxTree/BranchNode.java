package SyntaxTree;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class BranchNode implements StmtNode {
    // 1. 针对if <Cond> <Stmt> else <Stmt>
    private ExpNode cond;
    private StmtNode ifStmt;
    public StmtNode elseStmt = null;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

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
    // 3. 专用于对VoidFunc类型函数的if-else分支检查
    public void checkForError(SyntaxType funcDefSyntaxType) {
        // 1. <ifStmt>如果是<BlockNode>就给<BlockNode>查
        // 1. <ifStmt>如果是<ReturnNode>就检查<ReturnNode>的是否具有返回值
        if(ifStmt instanceof BlockNode){
            ((BlockNode)ifStmt).checkForError(funcDefSyntaxType);
        } else if (ifStmt instanceof ReturnNode && ((ReturnNode) ifStmt).hasExpNode()) {
            errorHandler.addError(new ErrorRecord(((ReturnNode) ifStmt).getLineNumber(), 'f'));
        }

        // 2. <elseStmt>如果是<BlockNode>就给<BlockNode>查
        // 2. <elseStmt>如果是<ReturnNode>就检查<ReturnNode>的是否具有返回值
        if(elseStmt instanceof BlockNode){
            ((BlockNode)elseStmt).checkForError(funcDefSyntaxType);
        } else if (elseStmt instanceof ReturnNode && ((ReturnNode) elseStmt).hasExpNode()) {
            errorHandler.addError(new ErrorRecord(((ReturnNode) elseStmt).getLineNumber(), 'f'));
        }
    }
}
