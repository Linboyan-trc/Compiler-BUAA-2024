package SyntaxTree;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class GetIntNode implements StmtNode {
    // 1. <LVal>
    private LValNode lValNode;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造
    public GetIntNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    // 3. 检查
    // 3. 不能对常量进行修改，对常量进行修改为h类错误
    public void checkForError(SymbolTable symbolTable) {
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
}
