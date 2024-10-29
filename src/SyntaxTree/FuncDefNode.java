package SyntaxTree;

import Lexer.Pair;

import java.util.LinkedList;

public class FuncDefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <FuncDefNode> = [变量类型:需要新开一个枚举类] + Pair:IDENFR + 参数列表 + 块
    private String funcDefType;
    private Pair pair;
    private LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
    private BlockNode blockNode;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public FuncDefNode(String funcDefType, Pair pair) {
        this.funcDefType = funcDefType;
        this.pair = pair;
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

    public Pair getPair() {
        return pair;
    }

    public LinkedList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }
}