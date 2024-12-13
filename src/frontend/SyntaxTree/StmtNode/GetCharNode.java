package frontend.SyntaxTree.StmtNode;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.DefNode;
import frontend.SyntaxTree.ExpNode.ExpNode;
import frontend.SyntaxTree.ExpNode.LValNode;
import midend.MidCode.MidCode.Move;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;
import midend.MidCode.MidCode.*;
import midend.MidCode.Operate.*;
import static midend.MidCode.Operate.BinaryOperate.BinaryOp.*;

public class GetCharNode implements StmtNode {
    // 1. <LVal>
    private final SymbolTable symbolTable;
    private LValNode lValNode;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造
    public GetCharNode(SymbolTable symbolTable, LValNode lValNode) {
        this.symbolTable = symbolTable;
        this.lValNode = lValNode;
    }

    // 3. 检查
    // 3. 不能对常量进行修改，对常量进行修改为h类错误
    public void checkForError() {
        DefNode defNode;
        if((defNode = symbolTable.getVariable(lValNode.getPair().getWord(),lValNode.getPair().getLineNumber())) != null) {
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
    public GetCharNode simplify() {
        lValNode = lValNode.compute();
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 获取左指IDENFR在符号表中的DefNode
        DefNode defNode = symbolTable.getVariable(lValNode.getPair().getWord(),lValNode.getPair().getLineNumber()).simplify();

        // 2. 获取作用域id
        int id = defNode.getScopeId();

        // 3. 获取符号表中变量对应的数组长度
        ExpNode length = defNode.getLength();

        // 4. 声明一个新的读一个Int的节点
        new CharGet();

        // 5. 如果变量是单变量
        if (defNode.getDefNodeType().isVariable()) {
            Word value = new Word(lValNode.getPair().getWord() + "@" + id);
            new Move(false, value, new Word("?"));
            return null;
        }

        // 5. 如果变量是数组
        else {
            // 5. 获取下标
            Value offset = lValNode.getLength().generateMidCode();
            Addr addr = new Addr();
            MidCodeTable.getInstance().addToMidCodes(
                new Assign(
                        true,
                        addr,
                        new BinaryOperate(ADD, new Addr(lValNode.getPair().getWord() + "@" + id), offset))
            );
            MidCodeTable.getInstance().addToVarInfo(addr, 1);
            new Store(addr, new Word("?"));
            return null;
        }
    }
}
