package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class FuncDefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <FuncDefNode> = [变量类型:需要新开一个枚举类] + Pair:IDENFR + 参数列表 + 块
    private SyntaxType funcDefType = null;
    private Pair pair = null;
    private LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
    private BlockNode blockNode = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public FuncDefNode(SyntaxType funcDefType) {
        this.funcDefType = funcDefType;
    }

    public void setPair(Pair pair) {
        this.pair = pair;
    }

    public void setFuncFParams(LinkedList<FuncFParamNode> funcFParamNodes) {
        this.funcFParamNodes = funcFParamNodes;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    // 2. get
    public SyntaxType getFuncDefType() {
        return funcDefType;
    }

    public Pair getPair() {
        return pair;
    }

    public LinkedList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    // 3. 检查
    // 3. 就是检查函数的funcDefType和<Block>的返回值是否能一致
    public void checkForError() {
        blockNode.checkForError(funcDefType);
    }
}