package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class FuncCallNode implements ExpNode {
    // 1. 函数名 + 参数
    private final SymbolTable symbolTable;
    private Pair pair;
    private LinkedList<ExpNode> arguments = new LinkedList<>();
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造 + 设置参数
    public FuncCallNode(SymbolTable symbolTable, Pair pair, LinkedList<ExpNode> arguments) {
        this.symbolTable = symbolTable;
        this.pair = pair;
        this.arguments = arguments;
    }

    // 3. 检查
    // 3. 包括:未定义名字，参数个数不匹配，参数个数匹配但是参数类型不匹配
    public void checkForError() {
        // 1. 根据调用的函数名找到函数
        FuncDefNode funcDefNode;
        // 2. 如果找不到，就是未定义的名字，c类错误
        if((funcDefNode = symbolTable.getFunction(pair.getWord())) == null) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'c'));
        }
        // 3. 如果参数个数不匹配，就是d类错误
        else if(funcDefNode.getFuncFParamNodes().size() != arguments.size()) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'd'));
        }
        // 4. 如果参数个数匹配，但是类型不匹配，就是e类错误
        // 4.1 包括funcDefNode.getFuncFParamNodes()中每个<FuncFParamNode>的类型
        // 4.2 arguments中每个<ExpNode>的类型
        else {
            for(int i = 0; i < arguments.size(); i++) {
                SyntaxType temp1 = arguments.get(i).getSyntaxType(symbolTable);
                SyntaxType temp2 = funcDefNode.getFuncFParamNodes().get(i).getDefNodeType();
                if((temp1.isArray() && temp2.isVariable()) ||
                        (temp1.isVariable() && temp2.isArray()) ||
                        (temp1.isCharArray() && temp2.isIntArray())||
                        (temp1.isIntArray() && temp2.isCharArray())) {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'e'));
                    break;
                }
            }
        }
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 找不到就null
        FuncDefNode funcDefNode = symbolTable.getFunction(pair.getWord());
        if(funcDefNode == null) {
            return null;
        }
        // 2. 找得到的话VoidFunc: Void
        // 2. 找得到的话IntFunc:  Int
        // 2. 找得到的话CharFunc: Char
        else {
            if(funcDefNode.getFuncDefType() == SyntaxType.VoidFunc) {
                return SyntaxType.Void;
            } else if (funcDefNode.getFuncDefType() == SyntaxType.IntFunc) {
                return SyntaxType.Int;
            } else {
                return SyntaxType.Char;
            }
        }
    }
}
