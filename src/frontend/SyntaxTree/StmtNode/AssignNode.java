package frontend.SyntaxTree.StmtNode;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.DefNode;
import frontend.SyntaxTree.ExpNode.ExpNode;
import frontend.SyntaxTree.ExpNode.LValNode;
import midend.MidCode.MidCode.Assign;
import midend.MidCode.MidCode.Move;
import midend.MidCode.MidCode.Store;
import midend.MidCode.Operate.BinaryOperate;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.*;
import static midend.MidCode.Operate.BinaryOperate.BinaryOp.*;

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

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 获取左指IDENFR在符号表中的DefNode
        DefNode defNode = symbolTable.getVariable(lValNode.getPair().getWord()).simplify();

        // 2. 获取作用域id
        int id = defNode.getScopeId();

        // 3. 获取数组长度
        ExpNode length = defNode.getLength();

        // 4. 右侧赋值生成中间代码
        // 4. NumberNode就是一个Value:Imm
        Value expValue = expNode.generateMidCode();

        // 5. 如果左值对应的变量是单变量
        if (length == null) {
            // 5. 创建一个赋值操作
            // 5. 如果左值对应的变量是char，要截断
            if (defNode.getDefNodeType().isCharType()) {
                Word value = new Word(lValNode.getPair().getWord() + "@" + id);
                expValue.truncTo8();
                new Move(false, value, expValue);
                return null;
            } else {
                Word value = new Word(lValNode.getPair().getWord() + "@" + id);
                new Move(false, value, expValue);
                return null;
            }
        }

        // 5. 如果左值对应的变量是数组
        else {
            // 5. 获取下标
            // 5. 存储在内存中
            Value offset = lValNode.getLength().generateMidCode();
            Addr addr = new Addr();
            new Assign(
                    true,
                    addr,
                    new BinaryOperate(ADD, new Addr(lValNode.getPair().getWord() + "@" + id), offset));
            if (defNode.getDefNodeType().isCharType()) {
                expValue.truncTo8();
                new Store(addr, expValue);
            } else {
                new Store(addr, expValue);
            }
            return null;
        }
    }
}
