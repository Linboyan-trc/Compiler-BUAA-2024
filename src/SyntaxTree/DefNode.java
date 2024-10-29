package SyntaxTree;

import Lexer.Pair;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = Pair:IDENFR + 维数 + 初始值
    private Pair identity;
    private LinkedList<ExpNode> dimensions = new LinkedList<>();
    private LinkedList<ExpNode> initValues = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(Pair identity) {
        this.identity = identity;
    }

    public void addDimension(ExpNode expNode) {
        dimensions.add(expNode);
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }

    // 2. get
    public Pair getIdentity() {
        return identity;
    }

    public LinkedList<ExpNode> getDimensions() {
        return dimensions;
    }
}
