package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.LabelTable.Label;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;
import midend.MidCode.MidCode.*;

public class BranchNode implements StmtNode {
    // 1. 针对if <Cond> <Stmt> else <Stmt>
    private final SymbolTable symbolTable;
    private ExpNode cond;
    private StmtNode ifStmt;
    public StmtNode elseStmt;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造 + 设置<Cond> + 设置<Stmt> + 设置<Stmt>
    public BranchNode(SymbolTable symbolTable, ExpNode cond, StmtNode thenStmt, StmtNode elseStmt) {
        this.symbolTable = symbolTable;
        this.cond = cond;
        this.ifStmt = thenStmt;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public StmtNode simplify() {
        // 1. 化简
        ExpNode simplifiedfCond = cond.simplify();
        StmtNode simplifiedIfStmt = ifStmt.simplify();
        StmtNode simplifiedElseStmt = elseStmt == null ? null : elseStmt.simplify();

        // 2. 条件是数字
        // 2. 选择两者之一返回
        if (simplifiedfCond instanceof NumberNode || simplifiedfCond instanceof CharacterNode) {
            long temp;
            if(simplifiedfCond instanceof NumberNode){
                temp = ((NumberNode)simplifiedfCond).getValue();
            } else {
                temp = ((CharacterNode)simplifiedfCond).getValue();
            }
            return temp == 0 ? simplifiedElseStmt == null ? new NopNode() : simplifiedElseStmt : simplifiedIfStmt;
        }

        // 3. 条件是BinaryNode
        // 3. 把一层if-else拆成两层if-else，使得每一层cond只有一个表达式
        else if (simplifiedfCond instanceof BinaryExpNode) {
            if (((BinaryExpNode) simplifiedfCond).getBinaryOp().isToken(AND)) {
                StmtNode newIfStmt = new BranchNode(symbolTable, ((BinaryExpNode) simplifiedfCond).getRightExp(), simplifiedIfStmt, simplifiedElseStmt).simplify();
                return new BranchNode(symbolTable, ((BinaryExpNode) simplifiedfCond).getLeftExp(), newIfStmt, simplifiedElseStmt).simplify();
            } else if (((BinaryExpNode) simplifiedfCond).getBinaryOp().isToken(OR)) {
                StmtNode newElse = new BranchNode(symbolTable, ((BinaryExpNode) simplifiedfCond).getRightExp(), simplifiedIfStmt, simplifiedElseStmt).simplify();
                return new BranchNode(symbolTable, ((BinaryExpNode) simplifiedfCond).getLeftExp(), simplifiedIfStmt, newElse).simplify();
            }
        }

        // 4. 返回节点
        return new BranchNode(symbolTable, simplifiedfCond, simplifiedIfStmt, simplifiedElseStmt);
    }

    @Override
    public Value generateMidCode() {
        // 1. 创建Label
        Label thenEndLabel = new Label();
        Value condValue = cond.generateMidCode();


        new Branch(Branch.BranchOp.EQ, condValue, new Imm(0), thenEndLabel);


        ifStmt.generateMidCode();


        if (elseStmt == null) {
            Nop thenEnd = new Nop();
            thenEndLabel.setMidCode(thenEnd);
        } else {
            Label elseEndLabel = new Label();
            new Jump(elseEndLabel);
            Nop thenEnd = new Nop();
            elseStmt.generateMidCode();
            Nop elseEnd = new Nop();
            thenEndLabel.setMidCode(thenEnd);
            elseEndLabel.setMidCode(elseEnd);
        }
        return null;
    }
}
