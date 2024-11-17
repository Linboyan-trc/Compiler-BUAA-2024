package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

public class LValNode implements ExpNode {
    // 1. Pair + 维数
    private final SymbolTable symbolTable;
    private Pair pair;
    private ExpNode length = null;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造 + 设置pair + 设置length
    public LValNode(SymbolTable symbolTable, Pair pair, ExpNode length) {
        this.symbolTable = symbolTable;
        this.pair = pair;
        this.length = length;
    }

    // 3. get
    public Pair getPair() {
        return pair;
    }

    // 4. 检查
    // 4. 使用未命名变量，为c类错误
    public void checkForError() {
        if(symbolTable.getVariable(pair.getWord()) == null) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'c'));
        }
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 首先在符号表中获取节点
        DefNode defNode = symbolTable.getVariable(pair.getWord());
        // 2. 节点自带类型
        // 2. 对于左值表达式: a, a[10]等
        // 2. 如果length = null,那么类型就和Pair一致
        // 2. 如果length != null,那么类型就是ConstInt, ConstChar, Int, Char中的一种
        if(length == null) {
            return defNode.getDefNodeType();
        } else {
            SyntaxType arrayType = defNode.getDefNodeType();
            switch (arrayType) {
                case ConstIntArray:
                    return SyntaxType.ConstInt;
                case ConstCharArray:
                    return SyntaxType.ConstChar;
                case IntArray:
                    return SyntaxType.Int;
                case CharArray:
                    return SyntaxType.Char;
                default:
                    return null;
            }
        }
    }
}
