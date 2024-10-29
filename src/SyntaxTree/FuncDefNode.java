package SyntaxTree;

import Lexer.Pair;

public class FuncDefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <FuncDefNode> = [变量类型:需要新开一个枚举类] + Pair:IDENFR + 参数列表 + 块
    private String funcDefType;
    private Pair identity;
    private LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
    private BlockNode blockNode;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public FuncDefNode(String funcDefType, Pair identity) {
        this.funcDefType = funcDefType;
        this.identity = identity;
    }

    public void setFuncFParams(LinkedList<FuncFParamNode> funcFParamNodes) {
        this.funcFParamNodes = funcFParamNodes;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    // 2. get
    public String getFuncDefType() {
        return funcDefType;
    }

    public Pair getIdent() {
        return identity;
    }

    public LinkedList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }
}