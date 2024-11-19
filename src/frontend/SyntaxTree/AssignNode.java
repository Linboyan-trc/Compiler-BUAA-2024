package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public class AssignNode implements StmtNode {
    // 1. <LValNode> + <ExpNode>
    private final SymbolTable symbolTable;
    private LValNode lValNode;
    private ExpNode expNode;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2.
    public AssignNode(SymbolTable symbolTable, LValNode lValNode, ExpNode toExpNode) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
        this.expNode = toExpNode;
    }

    // 3. 检查
    // 3. 不能对常量进行修改，对常量进行修改为h类错误
    public void checkForError() {
        DefNode defNode;
        if((defNode = symbolTable.getVariable(lValNode.getPair().getWord())) != null) {
            if(defNode.getDefNodeType() == SyntaxType.ConstInt ||
                    defNode.getDefNodeType() == SyntaxType.ConstIntArray ||
                    defNode.getDefNodeType() == SyntaxType.ConstChar ||
                    defNode.getDefNodeType() == SyntaxType.ConstCharArray) {
                errorHandler.addError(new ErrorRecord(lValNode.getPair().getLineNumber(), 'h'));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public AssignNode simplify() {
        lValNode = lValNode.compute();
        expNode = expNode.simplify();
        return this;
    }
}
