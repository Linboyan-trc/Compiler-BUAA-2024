package SyntaxTree;

import java.util.LinkedList;

public class DeclNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DeclNode> = [变量类型:需要新开一个枚举类] + <DefNode>
    private String declNodeType;
    private LinkedList<DefNode> defNodes = new LinkedList<>();

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DeclNode(String declNodeType) {
        this.declNodeType = declNodeType;
    }

    public void addDefNode(DefNode defNode) {
        defNodes.add(defNode);
    }

    // 2. get
    public String getDeclNodeType() {
        return declNodeType;
    }

    public LinkedList<DefNode> getDefNodes() {
        return defNodes;
    }
}