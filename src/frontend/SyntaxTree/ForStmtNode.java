package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.ExpNode.ExpNode;
import frontend.SyntaxTree.ExpNode.LValNode;
import frontend.Lexer.Pair;
import midend.MidCode.Value.Value;

public class ForStmtNode implements SyntaxNode {
    // 1. <ForStmt> = <LVal> '=' <Exp>
    private final SymbolTable symbolTable;
    private LValNode lValNode;
    private ExpNode expNode;
    private boolean isDef;
    private DeclNode varDef;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2.
    public ForStmtNode(SymbolTable symbolTable, LValNode lValNode, ExpNode expNode, boolean isDef, DeclNode varDef) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
        this.expNode = expNode;
        this.isDef = isDef;
        this.varDef = varDef;
    }

    public LValNode getLValNode() {
        return lValNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public boolean isDef() {
        return isDef;
    }

    public DeclNode getVarDef() {
        return varDef;
    }

    // 3. 检查
    // 3. 不能对常量进行修改，对常量进行修改为h类错误
    public void checkForError() {
        DefNode defNode;
        if(lValNode != null) {
            if ((defNode = symbolTable.getVariable(lValNode.getPair().getWord(), lValNode.getPair().getLineNumber())) != null) {
                if (defNode.getDefNodeType() == SyntaxType.ConstInt ||
                        defNode.getDefNodeType() == SyntaxType.ConstIntArray ||
                        defNode.getDefNodeType() == SyntaxType.ConstChar ||
                        defNode.getDefNodeType() == SyntaxType.ConstCharArray) {
                    errorHandler.addError(new ErrorRecord(lValNode.getPair().getLineNumber(), 'h'));
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ForStmtNode simplify() {
        if(lValNode != null) {
            lValNode = lValNode.compute();
            expNode = expNode.simplify();
            return this;
        } else {
            varDef = varDef.simplify();
            return this;
        }
    }

    @Override
    public Value generateMidCode(){
        return null;
    }
}
