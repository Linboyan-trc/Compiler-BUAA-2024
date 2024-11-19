package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

import java.util.LinkedList;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public LoopNode simplify() {
        // 1. 化简
        ExpNode simCond = cond.simplify();
        ForStmtNode simForStmtNodeFirst = null;
        ForStmtNode simForStmtNodeSecond = null;
        if(this.forStmtNodeFirst != null) {
            simForStmtNodeFirst = forStmtNodeFirst.simplify();
        }
        if(this.forStmtNodeSecond != null) {
            simForStmtNodeSecond = forStmtNodeSecond.simplify();
        }

        // 2. 是0直接返回空ForNode
        if(simCond instanceof NumberNode || simCond instanceof CharacterNode){
            long value;
            if (simCond instanceof NumberNode) {
                value = ((NumberNode) simCond).getValue();
            } else {
                value = ((CharacterNode) simCond).getValue();
            }
            if(value == 0){
                return new LoopNode(symbolTable, new NumberNode(1), new BreakNode(symbolTable));
            }
        }

        // 3. 否则化成while语句:
        // while(1){
        //  assginNode
        //  if(cond){
        //      stmt
        //      assignNode
        //  } else {
        //      breakNode
        //  }
        // }
        // 3.1 as1, as2
        StmtNode as1 = new NopNode();
        stmt = stmt.simplify();
        StmtNode as2 = new NopNode();
        if(simForStmtNodeFirst != null) {
            as1 = (StmtNode) new AssignNode(symbolTable, simForStmtNodeFirst.getLValNode(), simForStmtNodeFirst.getExpNode());
        }
        if(simForStmtNodeSecond != null) {
            as2 = (StmtNode) new AssignNode(symbolTable, simForStmtNodeSecond.getLValNode(), simForStmtNodeSecond.getExpNode());
        }
        // 3.2 LinkedList<BlockItemNode> for if
        LinkedList<BlockItemNode> bi1 = new LinkedList<>();
        bi1.add(stmt);
        bi1.add(as2);
        BlockNode bk = new BlockNode(symbolTable, bi1, 0);
        BranchNode bn = new BranchNode(symbolTable, simCond, bk, new BreakNode(symbolTable));
        // 3.3 LinkedList<BlockItemNode> for while
        LinkedList<BlockItemNode> bi2 = new LinkedList<>();
        bi2.add(as1);
        bi2.add(bn);
        BlockNode bk2 = new BlockNode(symbolTable, bi2, 0);

        return new LoopNode(symbolTable, new NumberNode(1), bk2);
    }
}
