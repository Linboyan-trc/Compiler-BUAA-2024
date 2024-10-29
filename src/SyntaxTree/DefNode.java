package SyntaxTree;

import Lexer.Pair;

import java.util.LinkedList;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = Parent + Pair:IDENFR + 维数 + 初始值
    private DeclNode parent = null;
    private Pair pair;
    private ExpNode length = null;
    private LinkedList<ExpNode> initValues = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(DeclNode parent, Pair pair) {
        this.parent = parent;
        this.pair = pair;
    }

    public DefNode(Pair pair) {
        this.pair = pair;
    }

    public void setLength(ExpNode expNode) {
        this.length = expNode;
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }


    // 2. get
    public DeclNode getParent() {
        return parent;
    }

    public Pair getPair() {
        return pair;
    }

    public ExpNode getLength() {
        return length;
    }
}
