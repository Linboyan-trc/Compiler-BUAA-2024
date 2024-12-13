package frontend.SyntaxTree.ExpNode;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.StmtNode.*;
import frontend.SyntaxTree.DefNode;
import midend.MidCode.MidCode.Assign;
import midend.MidCode.MidCode.Load;
import midend.MidCode.MidCode.Move;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Operate.BinaryOperate;
import midend.MidCode.Value.*;
import static midend.MidCode.Operate.BinaryOperate.BinaryOp.*;

public class LValNode implements ExpNode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. Pair + 维数:当指定下标的时候length为空
    // 2. LValNode只会为一个单个的变量，或者指定了下标的数组中的元素
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

    public ExpNode getLength() {
        return length;
    }

    // 4. 检查
    // 4. 使用未命名变量，为c类错误
    public void checkForError() {
        if(symbolTable.getVariable(pair.getWord(),pair.getLineNumber()) == null) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'c'));
        }
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 首先在符号表中获取节点
        DefNode defNode = symbolTable.getVariable(pair.getWord(),pair.getLineNumber());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ExpNode simplify() {
        // 1. 对维数中的ExpNode化简
        if(length != null) {
            length = length.simplify();
        }

        // 2. 如果是数组，如果每个length都是现成的数字或字符
        if (length instanceof NumberNode ||length instanceof CharacterNode) {

            // 3. 然后去符号表中找到这个变量
            DefNode defNode = symbolTable.getVariable(pair.getWord(),pair.getLineNumber());

            // 4. 如果这个变量是常量，说明一定为const int a[10][10] = {...}这种
            if (defNode.isFinal()) {

                // 5. 如果是的话说明取出的是单个元素，直接获取这个元素的值返回成一个ExpNode
                return defNode.getValue(length);
            }
        }

        // 3. 考虑单变量
        else {
            // 3.1 当变量是常量，并且不是数组的时候，直接获取值
            DefNode defNode = symbolTable.getVariable(pair.getWord(),pair.getLineNumber());
            if (defNode.isFinal() && defNode.getDefNodeType().isVariable()) {
                return defNode.getValue(null);
            }
        }

        return this;
    }

    // 2. 化简length
    public LValNode compute() {
        if(length != null) {
            length = length.simplify();
        }
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 获取在符号表中的变量
        DefNode defNode = symbolTable.getVariable(pair.getWord(),pair.getLineNumber()).simplify();

        // 2. 获取作用域id
        int id = defNode.getScopeId();

        // 3. 获取长度
        ExpNode length = defNode.getLength();

        // 4. 如果是单变量，添加一个Word
        // 4. 添加一个赋值语句，从此变量加载到一个临时变量
        if (defNode.getDefNodeType().isVariable()) {
            Word value = new Word();
            MidCodeTable.getInstance().addToMidCodes(
                new Move(true, value, new Word(pair.getWord() + "@" + id))
            );
            MidCodeTable.getInstance().addToVarInfo(value, 1);
            return value;
        }

        // 5. 此变量为一维数组
        else {
            // 5. 如果此LValNode没有指定length，说明取此变量对应的数组的地址
            if (this.length == null) {
                Addr value = new Addr();
                MidCodeTable.getInstance().addToMidCodes(
                    new Move(true, value, new Addr(pair.getWord() + "@" + id))
                );
                MidCodeTable.getInstance().addToVarInfo(value, 1);
                return value;
            }

            // 5. 如果此LValNode指定了length，就需要返回一个加载数组中值的节点
            else {
                Value offsetValue = this.length.generateMidCode();
                Addr addr = new Addr();
                MidCodeTable.getInstance().addToMidCodes(
                    new Assign(
                            true,
                            addr,
                            new BinaryOperate(ADD, new Addr(pair.getWord() + "@" + id), offsetValue))
                );
                MidCodeTable.getInstance().addToVarInfo(addr, 1);
                Word value = new Word();
                MidCodeTable.getInstance().addToMidCodes(
                    new Load(true, value, addr)
                );
                MidCodeTable.getInstance().addToVarInfo(value, 1);
                return value;
            }
        }
    }
}
