package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.SyntaxTable.SymbolTable;
import frontend.Lexer.Token;

public class LoopNode implements StmtNode {
    private final SymbolTable symbolTable;
    private final ExpNode cond;
    private final StmtNode loopStmt;

    public LoopNode(SymbolTable symbolTable,
                    ExpNode loopCond,
                    StmtNode loopStmt) {
        this.symbolTable = symbolTable;
        this.cond = loopCond;
        this.loopStmt = loopStmt;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public LoopNode simplify() {
        return this;
    }
}
