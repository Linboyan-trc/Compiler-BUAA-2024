package SyntaxTree;

import java.util.LinkedList;

public class CompUnit {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <CompUnit> = <DeclNode> + <FuncDefNode> + <FuncDefNode:mainFuncDefNode>
    private LinkedList<DeclNode> declNodes = new LinkedList<>();
    private LinkedList<FuncDefNode> funcDefNodes = new LinkedList<>();
    private FuncDefNode mainFuncDefNode;

////////////////////////////////////////////////////////////////////////////////////////////////////
    public CompUnit() {
    }

    public void addDeclNode(DeclNode declNode) {
        declNodes.add(declNode);
    }

    public void addFuncDefNode(FuncDefNode funcDefNode) {
        funcDefNodes.add(funcDefNode);
    }

    public void setMainFuncDefNode(FuncDefNode mainFuncDefNode) {
        this.mainFuncDefNode = mainFuncDefNode;
    }
}
