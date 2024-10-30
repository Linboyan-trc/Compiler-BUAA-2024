package SyntaxTree;

import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DeclNode implements BlockItemNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DeclNode> = <DefNode>
    private LinkedList<DefNode> defNodes = new LinkedList<>();

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DeclNode() { }

    public void addDefNode(DefNode defNode) {
        defNodes.add(defNode);
    }

    // 2. get
    public LinkedList<DefNode> getDefNodes() {
        return defNodes;
    }
}