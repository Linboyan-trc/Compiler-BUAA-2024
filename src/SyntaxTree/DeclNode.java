package SyntaxTree;

import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DeclNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DeclNode> = [变量类型:需要新开一个枚举类] + <DefNode>
    private SyntaxType declNodeType;
    private LinkedList<DefNode> defNodes = new LinkedList<>();

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DeclNode(SyntaxType declNodeType) {
        this.declNodeType = declNodeType;
    }

    public void addDefNode(DefNode defNode) {
        defNodes.add(defNode);
    }

    public void toArray() {
        switch (declNodeType) {
            case ConstChar:
                declNodeType = SyntaxType.ConstCharArray;
                break;
            case ConstInt:
                declNodeType = SyntaxType.ConstIntArray;
                break;
            case Char:
                declNodeType = SyntaxType.CharArray;
                break;
            case Int:
                declNodeType = SyntaxType.IntArray;
                break;
            default:
                break;
        }
    }

    // 2. get
    public SyntaxType getDeclNodeType() {
        return declNodeType;
    }

    public LinkedList<DefNode> getDefNodes() {
        return defNodes;
    }
}