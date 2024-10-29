package SyntaxTree;

import Lexer.Pair;

import java.util.LinkedList;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = Pair:IDENFR + 维数 + 初始值
    private Pair pair;
    private LinkedList<ExpNode> dimensions = new LinkedList<>();
    private LinkedList<ExpNode> initValues = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(Pair pair) {
        this.pair = pair;
    }

    public void addDimension(ExpNode expNode) {
        dimensions.add(expNode);
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }

    // 2. get
    public Pair getPair() {
        return pair;
    }

    public LinkedList<ExpNode> getDimensions() {
        return dimensions;
    }
}
