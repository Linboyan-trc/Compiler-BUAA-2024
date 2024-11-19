package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public class GetIntNode implements StmtNode {
    // 1. <LVal>
    private final SymbolTable symbolTable;
    private LValNode lValNode;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造
    public GetIntNode(SymbolTable symbolTable, LValNode lValNode) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
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
    public GetIntNode simplify() {
        lValNode = lValNode.compute();
        return this;
    }
}
