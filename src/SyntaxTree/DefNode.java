package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = Parent + Pair:IDENFR + 维数 + 初始值
    private SyntaxType defNodeType = null;
    private Pair pair;
    private ExpNode length = null;
    private LinkedList<ExpNode> initValues = null;
    private Pair initValueForSTRCON = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(SyntaxType defNodeType, Pair pair) {
        this.defNodeType = defNodeType;
        this.pair = pair;
    }

    public void setLength(ExpNode expNode) {
        this.length = expNode;
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }

    public void setInitValueForSTRCON(Pair initValueForSTRCON) {
        this.initValueForSTRCON = initValueForSTRCON;
    }

    public void toArray() {
        switch (defNodeType) {
            case ConstChar:
                defNodeType = SyntaxType.ConstCharArray;
                break;
            case ConstInt:
                defNodeType = SyntaxType.ConstIntArray;
                break;
            case Char:
                defNodeType = SyntaxType.CharArray;
                break;
            case Int:
                defNodeType = SyntaxType.IntArray;
                break;
            default:
                break;
        }
    }

    // 2. get
    public Pair getPair() {
        return pair;
    }

    public ExpNode getLength() {
        return length;
    }

    public SyntaxType getDefNodeType() {
        return defNodeType;
    }
}
