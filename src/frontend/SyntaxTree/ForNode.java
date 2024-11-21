package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.MidCode.Value.Value;

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
    public BlockNode simplify() {
        // 1. 化简
        ExpNode simCond = null;
        ForStmtNode simForStmtNodeFirst = null;
        ForStmtNode simForStmtNodeSecond = null;
        if(cond != null) {
            simCond = cond.simplify();
        }
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
                return new BlockNode(symbolTable, new LinkedList<>(),0);
            }
        }

        // 2. 是null就直接对simCond至数字1
        if(simCond == null) {
            simCond = new NumberNode(1);
        }

        // 3. 否则化成while语句:
        // {
        //  assginNode
        //  while(1){
        //      if(cond){
        //          stmt(递归寻找continue，递归的过程中跳过fornode，如果有continue就要在continue前插入一个assginNode)
        //      } else {
        //          breakNode
        //      }
        //      assignNode
        //  }
        // }
        // 3.1 as1
        StmtNode as1 = new NopNode();
        if(simForStmtNodeFirst != null) {
            as1 = (StmtNode) new AssignNode(symbolTable, simForStmtNodeFirst.getLValNode(), simForStmtNodeFirst.getExpNode());
        }

        // 3.2 as2
        StmtNode as2 = new NopNode();
        if(simForStmtNodeSecond != null) {
            as2 = (StmtNode) new AssignNode(symbolTable, simForStmtNodeSecond.getLValNode(), simForStmtNodeSecond.getExpNode());
        }

        // 3.3 stmt
        // 3.3 单节点直接包装
        // 3.3 多节点调用方法
        if(stmt instanceof ContinueNode){
            LinkedList<BlockItemNode> bi = new LinkedList<>();
            bi.add(as2);
            bi.add(stmt);
            stmt = new BlockNode(symbolTable, bi, 0);
        } else {
            if (as2 instanceof AssignNode) {
                stmt.hasContinue((AssignNode) as2);
            }
        }
        stmt = stmt.simplify();

        // 3.4 if
        BranchNode bn = new BranchNode(symbolTable, simCond, stmt, new BreakNode(symbolTable));

        // 3.5 LinkedList<BlockItemNode> for while
        LinkedList<BlockItemNode> bi1 = new LinkedList<>();
        bi1.add(bn);
        bi1.add(as2);
        BlockNode bk = new BlockNode(symbolTable, bi1, 0);

        // 3.5 while
        LoopNode lp = new LoopNode(symbolTable, new NumberNode(1), bk);

        // 3.6 LinkedList<BlockItemNode> for block
        LinkedList<BlockItemNode> bi2 = new LinkedList<>();
        bi2.add(as1);
        bi2.add(lp);
        BlockNode bk2 = new BlockNode(symbolTable, bi2, 0);

        return bk2;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode(){
        return null;
    }
}
